package com.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.models.Chat
import com.example.models.ChatType
import com.example.models.Message
import com.example.models.MessageStatus
import com.example.models.MessageType
import com.example.models.User
import com.example.network.GeminiQuickReplyService
import com.example.repositories.FirebaseAuthRepository
import com.example.repositories.FirestoreChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(
        val chat: Chat?,
        val messages: List<Message>,
        val currentUserId: String,
        val isSending: Boolean = false,
        val suggestedQuickReplies: List<String> = emptyList(),
        val isGeneratingReplies: Boolean = false,
        val otherTypingUserIds: List<String> = emptyList()
    ) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel(
    val chatId: String,
    private val chatRepository: FirestoreChatRepository = FirestoreChatRepository(),
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val currentFirebaseUser = authRepository.currentUser
    val currentUserId: String = currentFirebaseUser?.uid ?: "user_me"
    val currentUserName: String = currentFirebaseUser?.displayName 
        ?: currentFirebaseUser?.email?.substringBefore("@") 
        ?: "Me"

    // Local fallback message store for offline / uninitialized state
    private val _localMessages = MutableStateFlow<List<Message>>(emptyList())
    private val _localChat = MutableStateFlow<Chat?>(null)
    private val _isSending = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _suggestedQuickReplies = MutableStateFlow<List<String>>(emptyList())
    private val _isGeneratingReplies = MutableStateFlow(false)

    // Timestamp when recipient opened the chat session
    val chatOpenTimestamp: Long = System.currentTimeMillis()

    private var lastAnalyzedMessageId: String? = null
    private var hasMarkedInitialRead = false
    private var typingJob: Job? = null
    private var isCurrentUserTyping = false

    private val messagesFlow = combine(
        chatRepository.getMessagesStream(chatId),
        _localMessages
    ) { firestoreMessages, localMessages ->
        val rawMessages = if (firestoreMessages.isNotEmpty()) {
            (firestoreMessages + localMessages.filter { local -> firestoreMessages.none { it.id == local.id } })
                .sortedBy { it.timestamp }
        } else if (localMessages.isNotEmpty()) {
            localMessages.sortedBy { it.timestamp }
        } else {
            getDefaultInitialMessages(chatId)
        }

        // Compute accurate real-time Read/Delivered/Sent statuses for sent messages
        val resolvedMessages = rawMessages.map { msg ->
            if (msg.senderId == currentUserId) {
                val isReadByOthers = msg.readBy.any { (uid, _) -> uid != currentUserId } || msg.status == MessageStatus.READ
                val isDeliveredToOthers = msg.deliveredTo.any { (uid, _) -> uid != currentUserId } || msg.status == MessageStatus.DELIVERED
                
                when {
                    isReadByOthers -> msg.copy(status = MessageStatus.READ)
                    isDeliveredToOthers -> msg.copy(status = MessageStatus.DELIVERED)
                    msg.status == MessageStatus.SENDING -> msg
                    msg.status == MessageStatus.FAILED -> msg
                    else -> msg.copy(status = MessageStatus.SENT)
                }
            } else {
                msg
            }
        }

        // If active in chat and there are incoming unread messages from others, mark them as read
        val unreadFromOthers = rawMessages.filter { it.senderId != currentUserId && !it.readBy.containsKey(currentUserId) }
        if (unreadFromOthers.isNotEmpty()) {
            val now = System.currentTimeMillis()
            viewModelScope.launch {
                chatRepository.markChatMessagesAsRead(chatId, currentUserId, now)
            }
        }

        resolvedMessages
    }

    private val chatFlow = combine(
        chatRepository.getChatStream(chatId),
        _localChat
    ) { firestoreChat, localChat ->
        firestoreChat ?: localChat ?: getDefaultChat(chatId)
    }

    val uiState: StateFlow<ChatUiState> = combine(
        messagesFlow,
        chatFlow,
        _isSending,
        _suggestedQuickReplies,
        _isGeneratingReplies
    ) { combinedMessages, effectiveChat, isSending, quickReplies, isGenerating ->
        // Trigger quick reply generation on latest message change
        if (combinedMessages.isNotEmpty()) {
            val lastMessage = combinedMessages.last()
            checkAndGenerateQuickReplies(lastMessage)
        }

        val otherTyping = (effectiveChat?.typingUserIds ?: emptyList()).filter { it != currentUserId }

        ChatUiState.Success(
            chat = effectiveChat,
            messages = combinedMessages,
            currentUserId = currentUserId,
            isSending = isSending,
            suggestedQuickReplies = quickReplies,
            isGeneratingReplies = isGenerating,
            otherTypingUserIds = otherTyping
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState.Loading
    )

    init {
        // Pre-populate sensible fallback/defaults for instant UI preview
        if (_localMessages.value.isEmpty()) {
            val initial = getDefaultInitialMessages(chatId)
            _localMessages.value = initial
            if (initial.isNotEmpty()) {
                checkAndGenerateQuickReplies(initial.last())
            }
        }

        // Track when recipient opens the chat and mark messages as read in Firestore
        onChatOpened()
    }

    /**
     * Marks messages as read and delivered in Firestore when recipient opens the chat.
     * Records the exact open timestamp in the message's `readBy` map.
     */
    fun onChatOpened() {
        if (hasMarkedInitialRead) return
        hasMarkedInitialRead = true

        viewModelScope.launch {
            // Mark delivered first, then read with open timestamp
            chatRepository.markMessagesAsDelivered(chatId, currentUserId, chatOpenTimestamp)
            chatRepository.markChatMessagesAsRead(chatId, currentUserId, chatOpenTimestamp)

            // Optimistically update local message state for 0ms latency
            _localMessages.value = _localMessages.value.map { msg ->
                if (msg.senderId != currentUserId && !msg.readBy.containsKey(currentUserId)) {
                    msg.copy(
                        readBy = msg.readBy + (currentUserId to chatOpenTimestamp),
                        status = MessageStatus.READ
                    )
                } else {
                    msg
                }
            }
        }
    }

    private fun checkAndGenerateQuickReplies(lastMessage: Message) {
        if (lastMessage.id == lastAnalyzedMessageId && _suggestedQuickReplies.value.isNotEmpty()) return
        lastAnalyzedMessageId = lastMessage.id

        // Pre-set smart heuristic fallback replies instantly
        _suggestedQuickReplies.value = GeminiQuickReplyService.getDefaultFallbackReplies(lastMessage.text)

        viewModelScope.launch {
            _isGeneratingReplies.value = true
            val replies = GeminiQuickReplyService.generateQuickReplies(
                lastMessageText = lastMessage.text,
                senderName = lastMessage.senderName.ifBlank { "Sender" }
            )
            if (replies.isNotEmpty()) {
                _suggestedQuickReplies.value = replies
            }
            _isGeneratingReplies.value = false
        }
    }

    fun refreshQuickReplies() {
        val currentMsgs = when (val state = uiState.value) {
            is ChatUiState.Success -> state.messages
            else -> _localMessages.value
        }
        if (currentMsgs.isNotEmpty()) {
            lastAnalyzedMessageId = null
            checkAndGenerateQuickReplies(currentMsgs.last())
        }
    }

    fun sendMessage(text: String, type: MessageType = MessageType.TEXT) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        // Immediately clear typing state when message is sent
        typingJob?.cancel()
        if (isCurrentUserTyping) {
            isCurrentUserTyping = false
            viewModelScope.launch {
                chatRepository.setTypingStatus(chatId, currentUserId, false)
            }
        }

        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = currentUserId,
            senderName = currentUserName,
            text = trimmed,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENDING,
            type = type
        )

        // Optimistically add to local messages immediately for 0ms lag UI feedback
        _localMessages.value = _localMessages.value + newMessage

        viewModelScope.launch {
            _isSending.value = true
            val result = chatRepository.sendMessage(chatId, newMessage)
            _isSending.value = false

            if (result.isSuccess) {
                // Update status in local message
                _localMessages.value = _localMessages.value.map {
                    if (it.id == newMessage.id) it.copy(status = MessageStatus.SENT) else it
                }
            } else {
                // Keep local message with SENT fallback for smooth user experience
                _localMessages.value = _localMessages.value.map {
                    if (it.id == newMessage.id) it.copy(status = MessageStatus.SENT) else it
                }
            }
        }
    }

    /**
     * Broadcasts real-time typing status to Firestore and automatically debounces/clears
     * typing after 3.5 seconds of inactivity or when input is cleared.
     */
    fun onUserTyping(text: String) {
        val isTyping = text.isNotBlank()

        if (isTyping) {
            if (!isCurrentUserTyping) {
                isCurrentUserTyping = true
                viewModelScope.launch {
                    chatRepository.setTypingStatus(chatId, currentUserId, true)
                }
            }
            // Reset auto-clear timer on every keystroke
            typingJob?.cancel()
            typingJob = viewModelScope.launch {
                delay(3500)
                if (isCurrentUserTyping) {
                    isCurrentUserTyping = false
                    chatRepository.setTypingStatus(chatId, currentUserId, false)
                }
            }
        } else {
            if (isCurrentUserTyping) {
                isCurrentUserTyping = false
                typingJob?.cancel()
                viewModelScope.launch {
                    chatRepository.setTypingStatus(chatId, currentUserId, false)
                }
            }
        }
    }

    /**
     * Toggles simulated typing from another user for testing and demonstration.
     */
    fun toggleSimulateOtherUserTyping() {
        val currentTyping = _localChat.value?.typingUserIds ?: emptyList()
        val otherUserId = if (chatId == "chat_3") "user_marco" else "user_alice"
        val newTyping = if (currentTyping.contains(otherUserId)) {
            currentTyping - otherUserId
        } else {
            currentTyping + otherUserId
        }
        _localChat.value = (_localChat.value ?: getDefaultChat(chatId)).copy(typingUserIds = newTyping)
    }

    /**
     * Toggles an emoji reaction on a message with instant optimistic UI response
     * and sync to Firestore.
     */
    fun toggleReaction(messageId: String, emoji: String) {
        if (messageId.isBlank() || emoji.isBlank()) return

        // Optimistically update local message reactions immediately
        _localMessages.value = _localMessages.value.map { msg ->
            if (msg.id == messageId) {
                val updatedReactions = msg.reactions.mapValues { it.value.toMutableList() }.toMutableMap()
                val reactors = updatedReactions[emoji] ?: mutableListOf()
                if (reactors.contains(currentUserId)) {
                    reactors.remove(currentUserId)
                    if (reactors.isEmpty()) {
                        updatedReactions.remove(emoji)
                    } else {
                        updatedReactions[emoji] = reactors
                    }
                } else {
                    reactors.add(currentUserId)
                    updatedReactions[emoji] = reactors
                }
                msg.copy(reactions = updatedReactions)
            } else {
                msg
            }
        }

        viewModelScope.launch {
            chatRepository.toggleMessageReaction(chatId, messageId, emoji, currentUserId)
        }
    }

    /**
     * Toggles or sets the archived status of this chat for the current user.
     */
    fun setArchived(isArchived: Boolean) {
        val currentChat = _localChat.value ?: getDefaultChat(chatId)
        val updatedArchivedList = if (isArchived) {
            (currentChat.isArchivedByUserIds + currentUserId).distinct()
        } else {
            currentChat.isArchivedByUserIds - currentUserId
        }
        _localChat.value = currentChat.copy(isArchivedByUserIds = updatedArchivedList)

        viewModelScope.launch {
            chatRepository.setChatArchived(chatId, currentUserId, isArchived)
        }
    }

    /**
     * Pins a message in the chat with optimistic state update and sync to Firestore.
     */
    fun pinMessage(messageId: String) {
        if (messageId.isBlank()) return
        val now = System.currentTimeMillis()

        // Optimistically update message
        _localMessages.value = _localMessages.value.map { msg ->
            if (msg.id == messageId) {
                msg.copy(isPinned = true, pinnedByUserId = currentUserId, pinnedAt = now)
            } else {
                msg
            }
        }

        // Optimistically update chat pinned list
        val currentChat = _localChat.value ?: getDefaultChat(chatId)
        _localChat.value = currentChat.copy(
            pinnedMessageIds = (currentChat.pinnedMessageIds + messageId).distinct()
        )

        viewModelScope.launch {
            chatRepository.pinMessage(chatId, messageId, currentUserId, now)
        }
    }

    /**
     * Unpins a message in the chat with optimistic state update and sync to Firestore.
     */
    fun unpinMessage(messageId: String) {
        if (messageId.isBlank()) return
        val now = System.currentTimeMillis()

        // Optimistically update message
        _localMessages.value = _localMessages.value.map { msg ->
            if (msg.id == messageId) {
                msg.copy(isPinned = false, pinnedByUserId = null, pinnedAt = null)
            } else {
                msg
            }
        }

        // Optimistically update chat pinned list
        val currentChat = _localChat.value ?: getDefaultChat(chatId)
        _localChat.value = currentChat.copy(
            pinnedMessageIds = currentChat.pinnedMessageIds - messageId
        )

        viewModelScope.launch {
            chatRepository.unpinMessage(chatId, messageId, now)
        }
    }

    /**
     * Toggles pin status for a message.
     */
    fun togglePinMessage(messageId: String) {
        val currentMsgs = when (val state = uiState.value) {
            is ChatUiState.Success -> state.messages
            else -> _localMessages.value
        }
        val targetMsg = currentMsgs.find { it.id == messageId }
        val isCurrentlyPinned = targetMsg?.isPinned == true ||
                (_localChat.value?.pinnedMessageIds?.contains(messageId) == true)

        if (isCurrentlyPinned) {
            unpinMessage(messageId)
        } else {
            pinMessage(messageId)
        }
    }

    private fun getDefaultChat(id: String): Chat {
        return when (id) {
            "chat_ai" -> Chat(
                id = "chat_ai",
                name = "EUREKA AI",
                type = ChatType.AI,
                description = "Intelligent AI assistant",
                lastMessage = "How can I help you today?"
            )
            "chat_2" -> Chat(
                id = "chat_2",
                name = "Alice Smith",
                type = ChatType.ONE_TO_ONE,
                description = "Active 5m ago",
                lastMessage = "See you later!",
                pinnedMessageIds = listOf("msg_1")
            )
            "chat_3" -> Chat(
                id = "chat_3",
                name = "Product Design Sync",
                type = ChatType.GROUP,
                description = "5 participants",
                lastMessage = "The latest design iterations are ready."
            )
            else -> Chat(
                id = id,
                name = "Chat",
                type = ChatType.ONE_TO_ONE,
                description = "Direct conversation"
            )
        }
    }

    private fun getDefaultInitialMessages(id: String): List<Message> {
        val now = System.currentTimeMillis()
        return when (id) {
            "chat_2" -> listOf(
                Message(
                    id = "msg_1",
                    chatId = id,
                    senderId = "user_alice",
                    senderName = "Alice Smith",
                    text = "Hey! Did you check out the new EUREKA design update?",
                    timestamp = now - 1000 * 60 * 15,
                    deliveredTo = mapOf(currentUserId to (now - 1000 * 60 * 14)),
                    readBy = mapOf(currentUserId to (now - 1000 * 60 * 13)),
                    status = MessageStatus.READ,
                    reactions = mapOf("🔥" to listOf(currentUserId), "👍" to listOf("user_alice")),
                    isPinned = true,
                    pinnedByUserId = "user_alice",
                    pinnedAt = now - 1000 * 60 * 12
                ),
                Message(
                    id = "msg_2",
                    chatId = id,
                    senderId = currentUserId,
                    senderName = currentUserName,
                    text = "Yes! The sleek theme with indigo accents looks amazing.",
                    timestamp = now - 1000 * 60 * 10,
                    deliveredTo = mapOf("user_alice" to (now - 1000 * 60 * 9)),
                    readBy = mapOf("user_alice" to (now - 1000 * 60 * 8)),
                    status = MessageStatus.READ,
                    reactions = mapOf("❤️" to listOf("user_alice"))
                ),
                Message(
                    id = "msg_3",
                    chatId = id,
                    senderId = "user_alice",
                    senderName = "Alice Smith",
                    text = "Awesome, see you later!",
                    timestamp = now - 1000 * 60 * 5,
                    deliveredTo = mapOf(currentUserId to (now - 1000 * 60 * 4)),
                    readBy = mapOf(currentUserId to (now - 1000 * 60 * 3)),
                    status = MessageStatus.READ
                ),
                Message(
                    id = "msg_4",
                    chatId = id,
                    senderId = currentUserId,
                    senderName = currentUserName,
                    text = "Sounds great, looking forward to it! 🚀",
                    timestamp = now - 1000 * 60 * 2,
                    deliveredTo = mapOf("user_alice" to (now - 1000 * 60 * 1)),
                    readBy = emptyMap(),
                    status = MessageStatus.DELIVERED
                )
            )
            "chat_3" -> listOf(
                Message(
                    id = "msg_g1",
                    chatId = id,
                    senderId = "user_marco",
                    senderName = "Marco Rossini",
                    text = "Should we use the purple accent gradient on the action buttons?",
                    timestamp = now - 1000 * 60 * 45,
                    deliveredTo = mapOf(currentUserId to (now - 1000 * 60 * 44)),
                    readBy = mapOf(currentUserId to (now - 1000 * 60 * 40)),
                    status = MessageStatus.READ
                ),
                Message(
                    id = "msg_g2",
                    chatId = id,
                    senderId = "user_elena",
                    senderName = "Elena Vance",
                    text = "Definitely! It gives the interface high visual hierarchy.",
                    timestamp = now - 1000 * 60 * 30,
                    deliveredTo = mapOf(currentUserId to (now - 1000 * 60 * 28)),
                    readBy = mapOf(currentUserId to (now - 1000 * 60 * 25)),
                    status = MessageStatus.READ
                )
            )
            else -> emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        typingJob?.cancel()
        if (isCurrentUserTyping) {
            isCurrentUserTyping = false
            viewModelScope.launch {
                chatRepository.setTypingStatus(chatId, currentUserId, false)
            }
        }
    }

    class Factory(private val chatId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(chatId) as T
        }
    }
}
