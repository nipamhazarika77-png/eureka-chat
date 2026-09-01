package com.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.Chat
import com.example.models.ChatType
import com.example.models.Message
import com.example.models.User
import com.example.repositories.FirestoreChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(
    private val chatRepository: FirestoreChatRepository = FirestoreChatRepository()
) : ViewModel() {

    private val _currentUser = MutableStateFlow(User(id = "user_1", displayName = "Nipam"))
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<Message>>(emptyList())
    val aiMessages: StateFlow<List<Message>> = _aiMessages.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _chats.value = listOf(
            Chat(
                id = "chat_ai",
                type = ChatType.AI,
                name = "EUREKA AI",
                lastMessage = "How can I help you today?",
                lastMessageTimestamp = System.currentTimeMillis()
            ),
            Chat(
                id = "chat_2",
                type = ChatType.ONE_TO_ONE,
                name = "Alice Smith",
                lastMessage = "See you later!",
                lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                unreadCount = 2
            ),
            Chat(
                id = "chat_3",
                type = ChatType.GROUP,
                name = "Project Team",
                lastMessage = "Bob: The designs are ready.",
                lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 60
            ),
            Chat(
                id = "chat_4",
                type = ChatType.ONE_TO_ONE,
                name = "Marcus Vance",
                lastMessage = "Can you send the updated specs?",
                lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
                unreadCount = 0
            ),
            Chat(
                id = "chat_5",
                type = ChatType.GROUP,
                name = "Design Sprint 2026",
                lastMessage = "Elena: Added the new wireframes.",
                lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                unreadCount = 1
            )
        )

        _aiMessages.value = listOf(
            Message(id = UUID.randomUUID().toString(), chatId = "chat_ai", senderId = "ai", text = "Hi! I am EUREKA AI. How can I help you today?", timestamp = System.currentTimeMillis() - 1000 * 60)
        )
        
        _messages.value = listOf(
            Message(id = UUID.randomUUID().toString(), chatId = "chat_2", senderId = "user_2", text = "Hey, are we still on for today?", timestamp = System.currentTimeMillis() - 1000 * 60 * 6),
            Message(id = UUID.randomUUID().toString(), chatId = "chat_2", senderId = "user_2", text = "See you later!", timestamp = System.currentTimeMillis() - 1000 * 60 * 5)
        )
    }

    /**
     * Checks whether a conversation is archived by the current user.
     */
    fun isChatArchived(chat: Chat): Boolean {
        return chat.isArchivedByUserIds.contains(_currentUser.value.id)
    }

    /**
     * Archives a conversation, hiding it from the main chat list and placing it in the Archived section.
     */
    fun archiveChat(chatId: String) {
        setChatArchived(chatId, true)
    }

    /**
     * Unarchives a conversation, restoring it to the main chat list.
     */
    fun unarchiveChat(chatId: String) {
        setChatArchived(chatId, false)
    }

    /**
     * Toggles the archive status of a conversation.
     */
    fun toggleArchiveChat(chatId: String) {
        val chat = _chats.value.find { it.id == chatId } ?: return
        val currentUserId = _currentUser.value.id
        val currentlyArchived = chat.isArchivedByUserIds.contains(currentUserId)
        setChatArchived(chatId, !currentlyArchived)
    }

    /**
     * Updates the archive status of a chat and synchronizes with Firestore.
     */
    fun setChatArchived(chatId: String, isArchived: Boolean) {
        val currentUserId = _currentUser.value.id
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                val updatedList = if (isArchived) {
                    (chat.isArchivedByUserIds + currentUserId).distinct()
                } else {
                    chat.isArchivedByUserIds - currentUserId
                }
                chat.copy(isArchivedByUserIds = updatedList)
            } else {
                chat
            }
        }

        viewModelScope.launch {
            chatRepository.setChatArchived(chatId, currentUserId, isArchived)
        }
    }

    fun sendMessage(chatId: String, text: String, isAi: Boolean = false) {
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = _currentUser.value.id,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        
        if (isAi) {
            _aiMessages.value = _aiMessages.value + newMessage
            simulateAiResponse(newMessage.text)
        } else {
            _messages.value = _messages.value + newMessage
        }
        
        // Update chat last message
        _chats.value = _chats.value.map {
            if (it.id == chatId) {
                it.copy(lastMessage = text, lastMessageTimestamp = newMessage.timestamp)
            } else {
                it
            }
        }
    }

    private fun simulateAiResponse(prompt: String) {
        viewModelScope.launch {
            try {
                // Add a "typing" state
                val typingMessage = Message(
                    id = "typing",
                    chatId = "chat_ai",
                    senderId = "ai",
                    text = "Thinking...",
                    timestamp = System.currentTimeMillis()
                )
                _aiMessages.value = _aiMessages.value + typingMessage

                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                
                // Build history
                val contents = _aiMessages.value.filter { it.id != "typing" }.map { msg ->
                    com.example.network.Content(
                        role = if (msg.senderId == "ai") "model" else "user",
                        parts = listOf(com.example.network.Part(text = msg.text))
                    )
                }

                val request = com.example.network.GenerateContentRequest(
                    contents = contents,
                    systemInstruction = com.example.network.Content(
                        parts = listOf(com.example.network.Part(text = "You are EUREKA AI, a helpful, secure, and advanced assistant integrated into the EUREKA messaging platform. Keep your answers concise, clear, and helpful."))
                    )
                )

                val response = com.example.network.RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I'm sorry, I couldn't generate a response."

                // Remove typing and add real response
                _aiMessages.value = _aiMessages.value.filter { it.id != "typing" }
                val aiResponse = Message(
                    id = UUID.randomUUID().toString(),
                    chatId = "chat_ai",
                    senderId = "ai",
                    text = responseText,
                    timestamp = System.currentTimeMillis()
                )
                _aiMessages.value = _aiMessages.value + aiResponse
                
                _chats.value = _chats.value.map {
                    if (it.id == "chat_ai") {
                        it.copy(lastMessage = aiResponse.text, lastMessageTimestamp = aiResponse.timestamp)
                    } else {
                        it
                    }
                }

            } catch (e: Exception) {
                _aiMessages.value = _aiMessages.value.filter { it.id != "typing" }
                val aiResponse = Message(
                    id = UUID.randomUUID().toString(),
                    chatId = "chat_ai",
                    senderId = "ai",
                    text = "Error connecting to AI: ${e.localizedMessage}. Did you set GEMINI_API_KEY?",
                    timestamp = System.currentTimeMillis()
                )
                _aiMessages.value = _aiMessages.value + aiResponse
            }
        }
    }
}
