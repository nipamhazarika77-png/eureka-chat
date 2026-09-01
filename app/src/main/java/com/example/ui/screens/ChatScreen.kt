package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.models.Chat
import com.example.models.ChatType
import com.example.models.Message
import com.example.models.MessageStatus
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.Purple
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextLightGray
import com.example.viewmodels.ChatUiState
import com.example.viewmodels.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val QUICK_REACTIONS = listOf("❤️", "👍", "🔥", "😂", "😮", "😢", "🎉", "👏")

val EMOJI_CATEGORIES = mapOf(
    "Smileys" to listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
        "🙂", "😉", "😍", "🥰", "😘", "😋", "😎", "🤩", "🥳", "🤔",
        "🤫", "🤭", "🤗", "😴", "🤯", "🥵", "🥶", "😱", "🥺", "🤠"
    ),
    "Gestures" to listOf(
        "👍", "👎", "👌", "✌️", "🤞", "🤟", "🤘", "🤙", "👈", "👉",
        "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "🤝", "🙏",
        "👏", "🙌", "👐", "🤲", "💪", "👊", "✊", "🤛", "🤜", "✍️"
    ),
    "Vibes" to listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "✨", "🔥",
        "💥", "🎉", "🎊", "🏆", "💯", "⭐", "🌟", "⚡", "🌈", "🎯"
    )
)

/**
 * ChatScreen displays real-time messages fetched from Firestore,
 * interactive emoji reaction badges, reaction popup bars, and
 * Gemini-powered AI quick reply suggestions analyzing the latest message.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(
        key = "chat_$chatId",
        factory = ChatViewModel.Factory(chatId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // State for message reaction selection and read receipt inspection
    var activeReactionMessageId by remember { mutableStateOf<String?>(null) }
    var showFullEmojiPickerForMessageId by remember { mutableStateOf<String?>(null) }
    var showInputEmojiPicker by remember { mutableStateOf(false) }
    var inspectMessageReceipt by remember { mutableStateOf<Message?>(null) }

    // State for keyword message search
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    when (val state = uiState) {
        is ChatUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        is ChatUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        is ChatUiState.Success -> {
            val messages = state.messages
            val chat = state.chat
            val isArchived = chat?.isArchivedByUserIds?.contains(state.currentUserId) == true

            // Pinned messages resolution
            val pinnedMessages = remember(messages, chat?.pinnedMessageIds) {
                messages.filter { it.isPinned || (chat?.pinnedMessageIds?.contains(it.id) == true) }
            }
            var isPinnedViewExpanded by remember { mutableStateOf(false) }
            var highlightedMessageId by remember { mutableStateOf<String?>(null) }
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            // Local keyword filtered messages
            val filteredMessages = remember(messages, searchQuery) {
                if (searchQuery.isBlank()) {
                    messages
                } else {
                    val query = searchQuery.trim()
                    messages.filter { msg ->
                        msg.text.contains(query, ignoreCase = true) ||
                        msg.senderName.contains(query, ignoreCase = true)
                    }
                }
            }

            // Scroll helper that centers the target message and highlights it temporarily
            val jumpToMessage: (String) -> Unit = { targetMessageId ->
                val index = filteredMessages.indexOfFirst { it.id == targetMessageId }
                if (index != -1) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(index)
                        highlightedMessageId = targetMessageId
                        kotlinx.coroutines.delay(1800)
                        if (highlightedMessageId == targetMessageId) {
                            highlightedMessageId = null
                        }
                    }
                }
            }

            // Auto scroll to bottom when new messages arrive (only when not actively searching)
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty() && !isSearchActive && searchQuery.isBlank()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    ChatTopAppBar(
                        chat = chat,
                        isArchived = isArchived,
                        onToggleArchive = { viewModel.setArchived(!isArchived) },
                        pinnedCount = pinnedMessages.size,
                        isPinnedViewExpanded = isPinnedViewExpanded,
                        onTogglePinnedView = { isPinnedViewExpanded = !isPinnedViewExpanded },
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onToggleSearch = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        },
                        onClearSearch = { searchQuery = "" },
                        matchCount = if (searchQuery.isNotBlank()) filteredMessages.size else null,
                        otherTypingUserIds = state.otherTypingUserIds,
                        onSimulateTyping = { viewModel.toggleSimulateOtherUserTyping() },
                        onBack = {
                            if (isSearchActive) {
                                isSearchActive = false
                                searchQuery = ""
                            } else {
                                onBack()
                            }
                        }
                    )
                },
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        // Gemini AI Quick Reply Suggestions (visible when not searching)
                        AnimatedVisibility(
                            visible = !isSearchActive && state.suggestedQuickReplies.isNotEmpty() && messages.isNotEmpty(),
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut()
                        ) {
                            SuggestedQuickRepliesSection(
                                replies = state.suggestedQuickReplies,
                                isGenerating = state.isGeneratingReplies,
                                onRefresh = { viewModel.refreshQuickReplies() },
                                onSelectReply = { selectedText ->
                                    inputText = selectedText
                                },
                                onSendDirectly = { selectedText ->
                                    viewModel.sendMessage(selectedText)
                                }
                            )
                        }

                        // Bottom message input controls
                        ChatBottomInputBar(
                            text = inputText,
                            onTextChange = {
                                inputText = it
                                viewModel.onUserTyping(it)
                            },
                            isSending = state.isSending,
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            onOpenEmojiPicker = {
                                showInputEmojiPicker = true
                            }
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Pinned messages header banner & expandable pinned view at the top of the chat
                    PinnedMessagesTopSection(
                        pinnedMessages = pinnedMessages,
                        isExpanded = isPinnedViewExpanded,
                        onToggleExpand = { isPinnedViewExpanded = !isPinnedViewExpanded },
                        onJumpToMessage = { targetId ->
                            jumpToMessage(targetId)
                        },
                        onUnpinMessage = { messageId ->
                            viewModel.unpinMessage(messageId)
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result = snackbarHostState.showSnackbar(
                                    message = "Message unpinned",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.pinMessage(messageId)
                                }
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (messages.isEmpty()) {
                            EmptyChatState(
                                chatName = chat?.name ?: "Chat",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (filteredMessages.isEmpty() && searchQuery.isNotBlank()) {
                            EmptySearchResultsState(
                                searchQuery = searchQuery,
                                onClearSearch = { searchQuery = "" },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                                    .testTag("chat_messages_list"),
                                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(
                                    items = filteredMessages,
                                    key = { it.id }
                                ) { message ->
                                    val isSentByMe = message.senderId == state.currentUserId
                                    val isReactionActive = activeReactionMessageId == message.id
                                    val isMsgPinned = message.isPinned || (chat?.pinnedMessageIds?.contains(message.id) == true)
                                    val isHighlighted = highlightedMessageId == message.id

                                    MessageItem(
                                        message = message,
                                        isSentByMe = isSentByMe,
                                        currentUserId = state.currentUserId,
                                        showSenderName = chat?.type == ChatType.GROUP && !isSentByMe,
                                        isReactionMenuOpen = isReactionActive,
                                        isPinned = isMsgPinned,
                                        isHighlighted = isHighlighted,
                                        searchQuery = searchQuery,
                                        onOpenReactionMenu = {
                                            activeReactionMessageId = if (isReactionActive) null else message.id
                                        },
                                        onTogglePin = {
                                            viewModel.togglePinMessage(message.id)
                                            activeReactionMessageId = null
                                            coroutineScope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                val result = snackbarHostState.showSnackbar(
                                                    message = if (isMsgPinned) "Message unpinned" else "Message pinned to top",
                                                    actionLabel = "Undo",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.togglePinMessage(message.id)
                                                }
                                            }
                                        },
                                        onReactionSelected = { emoji ->
                                            viewModel.toggleReaction(message.id, emoji)
                                            activeReactionMessageId = null
                                        },
                                        onOpenFullEmojiPicker = {
                                            showFullEmojiPickerForMessageId = message.id
                                            activeReactionMessageId = null
                                        },
                                        onOpenReceiptDetails = { msg ->
                                            inspectMessageReceipt = msg
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Full Emoji Picker Dialog for Message Reactions
            if (showFullEmojiPickerForMessageId != null) {
                val targetMessageId = showFullEmojiPickerForMessageId!!
                FullEmojiPickerDialog(
                    title = "React with Emoji",
                    onDismiss = { showFullEmojiPickerForMessageId = null },
                    onEmojiSelected = { emoji ->
                        viewModel.toggleReaction(targetMessageId, emoji)
                        showFullEmojiPickerForMessageId = null
                    }
                )
            }

            // Input Bar Emoji Picker Dialog
            if (showInputEmojiPicker) {
                FullEmojiPickerDialog(
                    title = "Insert Emoji",
                    onDismiss = { showInputEmojiPicker = false },
                    onEmojiSelected = { emoji ->
                        inputText += emoji
                        showInputEmojiPicker = false
                    }
                )
            }

            // Message Info & Read Receipt Details Dialog
            if (inspectMessageReceipt != null) {
                MessageReceiptDetailsDialog(
                    message = inspectMessageReceipt!!,
                    currentUserId = state.currentUserId,
                    onDismiss = { inspectMessageReceipt = null }
                )
            }
        }
    }
}

/**
 * Gemini Quick Replies Component displaying 3 smart options generated from the last message.
 */
@Composable
private fun SuggestedQuickRepliesSection(
    replies: List<String>,
    isGenerating: Boolean,
    onRefresh: () -> Unit,
    onSelectReply: (String) -> Unit,
    onSendDirectly: (String) -> Unit
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 4.dp)
        ) {
            // Header with AI indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "GEMINI SUGGESTED REPLIES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Purple
                    )
                    if (isGenerating) {
                        Spacer(modifier = Modifier.width(4.dp))
                        CircularProgressIndicator(
                            color = Purple,
                            strokeWidth = 1.5.dp,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(22.dp)
                        .testTag("refresh_quick_replies_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Regenerate Suggestions",
                        tint = TextLightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Horizontal row of 3 quick reply chips
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                replies.take(3).forEachIndexed { index, reply ->
                    QuickReplyChip(
                        replyText = reply,
                        index = index,
                        onTap = { onSelectReply(reply) },
                        onSendNow = { onSendDirectly(reply) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Quick Reply Pill Chip with tap-to-insert or instant-send actions.
 */
@Composable
private fun QuickReplyChip(
    replyText: String,
    index: Int,
    onTap: () -> Unit,
    onSendNow: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Purple.copy(alpha = 0.25f)),
        shadowElevation = 1.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onTap() }
            .testTag("quick_reply_chip_$index")
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = replyText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                maxLines = 1
            )

            // Direct Send shortcut button inside chip
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(DeepBlue.copy(alpha = 0.08f))
                    .clickable { onSendNow() }
                    .testTag("quick_reply_send_direct_$index"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Reply",
                    tint = DeepBlue,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

/**
 * Top App Bar with Contact info, Avatar, and action controls.
 * Supports switching into an active keyword search bar mode with matching counts and clear action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopAppBar(
    chat: Chat?,
    isArchived: Boolean = false,
    onToggleArchive: () -> Unit = {},
    pinnedCount: Int = 0,
    isPinnedViewExpanded: Boolean = false,
    onTogglePinnedView: () -> Unit = {},
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onClearSearch: () -> Unit,
    matchCount: Int?,
    otherTypingUserIds: List<String> = emptyList(),
    onSimulateTyping: () -> Unit = {},
    onBack: () -> Unit
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Surface(
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 2.dp
    ) {
        if (isSearchActive) {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onToggleSearch,
                        modifier = Modifier.testTag("close_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Search",
                            tint = DeepBlue
                        )
                    }
                },
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search in conversation...",
                                fontSize = 14.sp,
                                color = TextLightGray
                            )
                        },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = TextDark
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_search_input")
                    )
                },
                actions = {
                    if (matchCount != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (matchCount > 0) CyanAccent.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("search_results_count_chip")
                        ) {
                            Text(
                                text = if (matchCount == 1) "1 match" else "$matchCount matches",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (matchCount > 0) DeepBlue else TextGray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = onClearSearch,
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear Search",
                                tint = TextGray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        } else {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Contact Avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(DeepBlue.copy(alpha = 0.8f), Purple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (chat?.name?.take(1) ?: "C").uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Contact Name & Status
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = chat?.name ?: "Chat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val isOtherTyping = otherTypingUserIds.isNotEmpty()
                            Text(
                                text = if (isOtherTyping) "typing..." else if (chat?.type == ChatType.AI) "Online • EUREKA Assistant" else "Online",
                                fontSize = 11.sp,
                                fontWeight = if (isOtherTyping) FontWeight.Bold else FontWeight.Medium,
                                color = if (isOtherTyping) Color(0xFFD97706) else CyanAccent,
                                modifier = Modifier.testTag("chat_typing_indicator_status")
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepBlue
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onToggleSearch,
                        modifier = Modifier.testTag("chat_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search Messages",
                            tint = DeepBlue
                        )
                    }
                    IconButton(
                        onClick = onTogglePinnedView,
                        modifier = Modifier.testTag("chat_pinned_messages_toggle_btn")
                    ) {
                        if (pinnedCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFFD97706),
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = "$pinnedCount",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.testTag("pinned_count_badge")
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = "Pinned messages ($pinnedCount)",
                                    tint = if (isPinnedViewExpanded) Color(0xFFD97706) else DeepBlue
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned messages",
                                tint = if (isPinnedViewExpanded) Color(0xFFD97706) else DeepBlue.copy(alpha = 0.7f)
                            )
                        }
                    }
                    IconButton(onClick = { /* Voice Call */ }) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Voice Call",
                            tint = DeepBlue
                        )
                    }
                    IconButton(onClick = { /* Video Call */ }) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = "Video Call",
                            tint = DeepBlue
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.testTag("chat_more_options_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More Options",
                                tint = TextGray
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.PushPin,
                                            contentDescription = null,
                                            tint = DeepBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (pinnedCount > 0) "Pinned messages ($pinnedCount)" else "Pinned messages",
                                            fontSize = 14.sp,
                                            color = TextDark
                                        )
                                    }
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onTogglePinnedView()
                                },
                                modifier = Modifier.testTag("chat_pinned_messages_menu_item")
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isArchived) Icons.Filled.Unarchive else Icons.Filled.Archive,
                                            contentDescription = null,
                                            tint = DeepBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isArchived) "Unarchive chat" else "Archive chat",
                                            fontSize = 14.sp,
                                            color = TextDark
                                        )
                                    }
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onToggleArchive()
                                },
                                modifier = Modifier.testTag("chat_archive_toggle_menu_item")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
}

/**
 * Message Bubble representing sent or received messages with interactive reaction support and keyword highlighting.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun MessageItem(
    message: Message,
    isSentByMe: Boolean,
    currentUserId: String,
    showSenderName: Boolean,
    isReactionMenuOpen: Boolean,
    isPinned: Boolean = false,
    isHighlighted: Boolean = false,
    searchQuery: String = "",
    onOpenReactionMenu: () -> Unit,
    onTogglePin: () -> Unit = {},
    onReactionSelected: (String) -> Unit,
    onOpenFullEmojiPicker: () -> Unit,
    onOpenReceiptDetails: (Message) -> Unit = {}
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start
    ) {
        // Floating Quick Reaction & Action Bar when active
        AnimatedVisibility(
            visible = isReactionMenuOpen,
            enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut() + scaleOut()
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .testTag("quick_reaction_bar_${message.id}"),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QUICK_REACTIONS.forEach { emoji ->
                        val hasReacted = message.reactions[emoji]?.contains(currentUserId) == true
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (hasReacted) DeepBlue.copy(alpha = 0.15f) else Color.Transparent
                                )
                                .clickable { onReactionSelected(emoji) }
                                .testTag("reaction_option_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Plus button for full emoji catalog
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .clickable { onOpenFullEmojiPicker() }
                            .testTag("reaction_more_emojis_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "More Reactions",
                            tint = DeepBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Pin / Unpin quick action toggle button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isPinned) Color(0xFFFEF3C7) else Color(0xFFF1F5F9))
                            .clickable { onTogglePin() }
                            .testTag(if (isPinned) "unpin_message_btn_${message.id}" else "pin_message_btn_${message.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = if (isPinned) "Unpin message" else "Pin message",
                            tint = if (isPinned) Color(0xFFD97706) else DeepBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Message Bubble Container
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSentByMe) {
                // Quick add reaction button for sent messages
                IconButton(
                    onClick = onOpenReactionMenu,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = 4.dp)
                        .testTag("add_reaction_btn_${message.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SentimentSatisfiedAlt,
                        contentDescription = "React to message",
                        tint = TextLightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(min = 80.dp, max = 290.dp)
                    .then(
                        if (isHighlighted) {
                            Modifier.border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isSentByMe) 18.dp else 4.dp,
                                bottomEnd = if (isSentByMe) 4.dp else 18.dp
                            ))
                        } else Modifier
                    )
                    .shadow(
                        elevation = if (isSentByMe) 2.dp else 1.dp,
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isSentByMe) 18.dp else 4.dp,
                            bottomEnd = if (isSentByMe) 4.dp else 18.dp
                        )
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isSentByMe) 18.dp else 4.dp,
                            bottomEnd = if (isSentByMe) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (isSentByMe) {
                            Brush.horizontalGradient(listOf(DeepBlue, Purple))
                        } else {
                            Brush.linearGradient(listOf(Color.White, Color(0xFFF8F9FE)))
                        }
                    )
                    .combinedClickable(
                        onClick = { onOpenReceiptDetails(message) },
                        onLongClick = onOpenReactionMenu
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("message_bubble_${message.id}")
            ) {
                // Pinned badge indicator inside message bubble
                if (isPinned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .testTag("message_pinned_badge_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pinned message",
                            tint = if (isSentByMe) Color(0xFFFFE082) else Color(0xFFD97706),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "PINNED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = if (isSentByMe) Color(0xFFFFE082) else Color(0xFFD97706)
                        )
                    }
                }

                // Group chat sender name label
                if (showSenderName && message.senderName.isNotBlank()) {
                    Text(
                        text = message.senderName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Message Body Text with Keyword Search Highlighting
                val highlightedText = remember(message.text, searchQuery, isSentByMe) {
                    buildHighlightedMessageText(
                        text = message.text,
                        query = searchQuery,
                        isSentByMe = isSentByMe
                    )
                }

                Text(
                    text = highlightedText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = if (isSentByMe) Color.White else TextDark,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Message Footer: Timestamp & Delivery Status
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onOpenReceiptDetails(message) }
                        .padding(top = 2.dp)
                        .testTag("message_status_row_${message.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeString,
                        fontSize = 10.sp,
                        color = if (isSentByMe) Color.White.copy(alpha = 0.75f) else TextLightGray
                    )

                    if (isSentByMe) {
                        when (message.status) {
                            MessageStatus.SENDING -> {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = "Sending message",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(12.dp)
                                        .testTag("message_status_sending_${message.id}")
                                )
                            }
                            MessageStatus.SENT -> {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Sent to server",
                                    tint = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .size(13.dp)
                                        .testTag("message_status_sent_${message.id}")
                                )
                            }
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    imageVector = Icons.Filled.DoneAll,
                                    contentDescription = "Delivered to recipient",
                                    tint = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .size(13.dp)
                                        .testTag("message_status_delivered_${message.id}")
                                )
                            }
                            MessageStatus.READ -> {
                                Icon(
                                    imageVector = Icons.Filled.DoneAll,
                                    contentDescription = "Read by recipient",
                                    tint = CyanAccent,
                                    modifier = Modifier
                                        .size(13.dp)
                                        .testTag("message_status_read_${message.id}")
                                )
                            }
                            MessageStatus.FAILED -> {
                                Text(
                                    text = "!",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("message_status_failed_${message.id}")
                                )
                            }
                        }
                    }
                }
            }

            if (!isSentByMe) {
                // Quick add reaction button for received messages
                IconButton(
                    onClick = onOpenReactionMenu,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = 4.dp)
                        .testTag("add_reaction_btn_${message.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SentimentSatisfiedAlt,
                        contentDescription = "React to message",
                        tint = TextLightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Active Reactions Row underneath message bubble
        val activeReactions = message.reactions.filter { it.value.isNotEmpty() }
        if (activeReactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .testTag("reactions_row_${message.id}"),
                horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                activeReactions.forEach { (emoji, userIds) ->
                    val isReactedByMe = userIds.contains(currentUserId)
                    ReactionPill(
                        emoji = emoji,
                        count = userIds.size,
                        isReactedByMe = isReactedByMe,
                        onClick = { onReactionSelected(emoji) }
                    )
                }
            }
        }
    }
}

/**
 * Reaction Pill displaying an emoji badge with reaction count and active state.
 */
@Composable
private fun ReactionPill(
    emoji: String,
    count: Int,
    isReactedByMe: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(end = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("reaction_pill_$emoji"),
        shape = RoundedCornerShape(12.dp),
        color = if (isReactedByMe) DeepBlue.copy(alpha = 0.12f) else Color.White,
        border = BorderStroke(
            width = if (isReactedByMe) 1.5.dp else 1.dp,
            color = if (isReactedByMe) Purple else Color(0xFFE2E8F0)
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 13.sp
            )
            if (count > 0) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = if (isReactedByMe) FontWeight.Bold else FontWeight.Medium,
                    color = if (isReactedByMe) Purple else TextDark
                )
            }
        }
    }
}

/**
 * Full Emoji Catalog Picker Dialog for selecting any emoji.
 */
@Composable
private fun FullEmojiPickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = EMOJI_CATEGORIES.keys.toList()
    val currentCategory = categories.getOrElse(selectedCategoryIndex) { "Smileys" }
    val emojiList = EMOJI_CATEGORIES[currentCategory] ?: emptyList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = TextGray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
            ) {
                // Category Tabs
                TabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    containerColor = Color.Transparent,
                    contentColor = DeepBlue
                ) {
                    categories.forEachIndexed { index, catName ->
                        Tab(
                            selected = selectedCategoryIndex == index,
                            onClick = { selectedCategoryIndex = index },
                            text = {
                                Text(
                                    text = catName,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Emoji Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(emojiList) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .clickable { onEmojiSelected(emoji) }
                                .testTag("dialog_emoji_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

/**
 * Modern Floating Input Bar with Emoji, Attachment, and Gradient Send button.
 */
@Composable
private fun ChatBottomInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit,
    onOpenEmojiPicker: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded Input Container
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji Icon
                IconButton(
                    onClick = onOpenEmojiPicker,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("input_emoji_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SentimentSatisfiedAlt,
                        contentDescription = "Emojis",
                        tint = TextGray,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Text Input
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    placeholder = {
                        Text(
                            text = "Message...",
                            color = TextLightGray,
                            fontSize = 14.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSend() }
                    ),
                    singleLine = false,
                    maxLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    )
                )

                // Attachment Button
                IconButton(
                    onClick = { /* Attach File */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AttachFile,
                        contentDescription = "Attach file",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action / Send Button
            val isNotEmpty = text.isNotBlank()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isNotEmpty) {
                            Brush.linearGradient(listOf(DeepBlue, Purple))
                        } else {
                            Brush.linearGradient(listOf(DeepBlue.copy(alpha = 0.85f), Purple.copy(alpha = 0.85f)))
                        }
                    )
                    .clickable(enabled = isNotEmpty && !isSending) {
                        onSend()
                    }
                    .testTag("send_message_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isNotEmpty) Icons.AutoMirrored.Filled.Send else Icons.Filled.Mic,
                        contentDescription = if (isNotEmpty) "Send" else "Voice Message",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Empty Chat State placeholder.
 */
@Composable
private fun EmptyChatState(
    chatName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE8EAF6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SentimentSatisfiedAlt,
                contentDescription = null,
                tint = DeepBlue,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Say hello to $chatName!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "No messages yet. Send a message below to start the conversation.",
            fontSize = 13.sp,
            color = TextGray,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Detailed Message Info & Read Receipt Dialog showing exact timestamps
 * for Sent, Delivered, and Read statuses synced with Firestore.
 */
@Composable
private fun MessageReceiptDetailsDialog(
    message: Message,
    currentUserId: String,
    onDismiss: () -> Unit
) {
    val fullDateFormat = remember { SimpleDateFormat("MMM d, yyyy · HH:mm:ss", Locale.getDefault()) }
    val isSentByMe = message.senderId == currentUserId

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_receipt_dialog_btn")
            ) {
                Text(
                    text = "Close",
                    color = DeepBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = DeepBlue,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Message Info",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("message_receipt_details_sheet"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Message Content Card
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isSentByMe) "Sent by you" else "From ${message.senderName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSentByMe) Purple else DeepBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            color = TextDark,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Status Timeline Section
                Text(
                    text = "Read Receipt Status",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Sent Status
                    ReceiptStatusRow(
                        title = "Sent",
                        subtitle = fullDateFormat.format(Date(message.timestamp)),
                        icon = Icons.Filled.Check,
                        iconTint = Color(0xFF64748B),
                        backgroundColor = Color(0xFFF1F5F9),
                        isCompleted = true,
                        testTag = "receipt_row_sent"
                    )

                    // 2. Delivered Status
                    val deliveredTimestamp = message.deliveredTo.values.firstOrNull() ?: if (message.status == MessageStatus.DELIVERED || message.status == MessageStatus.READ) message.timestamp + 2000 else null
                    val isDelivered = deliveredTimestamp != null || message.status == MessageStatus.DELIVERED || message.status == MessageStatus.READ

                    ReceiptStatusRow(
                        title = "Delivered",
                        subtitle = if (deliveredTimestamp != null) fullDateFormat.format(Date(deliveredTimestamp)) else if (isDelivered) "Delivered to device" else "Pending delivery",
                        icon = Icons.Filled.DoneAll,
                        iconTint = if (isDelivered) Color(0xFF3B82F6) else Color(0xFF94A3B8),
                        backgroundColor = if (isDelivered) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        isCompleted = isDelivered,
                        testTag = "receipt_row_delivered"
                    )

                    // 3. Read Status
                    val readTimestamp = message.readBy.values.firstOrNull() ?: if (message.status == MessageStatus.READ) (deliveredTimestamp ?: message.timestamp) + 5000 else null
                    val isRead = readTimestamp != null || message.status == MessageStatus.READ

                    ReceiptStatusRow(
                        title = "Read",
                        subtitle = if (readTimestamp != null) fullDateFormat.format(Date(readTimestamp)) else if (isRead) "Read by recipient" else "Not yet opened",
                        icon = Icons.Filled.DoneAll,
                        iconTint = if (isRead) CyanAccent else Color(0xFF94A3B8),
                        backgroundColor = if (isRead) CyanAccent.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
                        isCompleted = isRead,
                        testTag = "receipt_row_read"
                    )
                }

                // Reactions Summary in Dialog if any
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Reactions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        message.reactions.forEach { (emoji, users) ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = emoji, fontSize = 14.sp)
                                    Text(
                                        text = "${users.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

@Composable
private fun ReceiptStatusRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    backgroundColor: Color,
    isCompleted: Boolean,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isCompleted) TextDark else TextLightGray
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isCompleted) TextGray else TextLightGray
            )
        }
    }
}

/**
 * Empty search results placeholder state when keyword filter returns 0 messages.
 */
@Composable
private fun EmptySearchResultsState(
    searchQuery: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(32.dp)
            .testTag("empty_search_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = DeepBlue,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No matching messages",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "No messages found matching \"$searchQuery\". Try searching with different keywords.",
            fontSize = 13.sp,
            color = TextGray,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onClearSearch,
            modifier = Modifier.testTag("clear_search_empty_btn")
        ) {
            Text(
                text = "Clear search",
                fontWeight = FontWeight.SemiBold,
                color = DeepBlue
            )
        }
    }
}

/**
 * Builds an AnnotatedString highlighting any occurrences of the search query keyword.
 */
private fun buildHighlightedMessageText(
    text: String,
    query: String,
    isSentByMe: Boolean
): AnnotatedString {
    val cleanQuery = query.trim()
    if (cleanQuery.isEmpty()) {
        return AnnotatedString(text)
    }
    val highlightBg = if (isSentByMe) Color(0xFFFFD54F) else Color(0xFFFFEB3B)
    val highlightColor = Color(0xFF1E293B)

    return buildAnnotatedString {
        var startIndex = 0
        while (startIndex < text.length) {
            val index = text.indexOf(cleanQuery, startIndex, ignoreCase = true)
            if (index == -1) {
                append(text.substring(startIndex))
                break
            }
            append(text.substring(startIndex, index))
            withStyle(
                SpanStyle(
                    background = highlightBg,
                    color = highlightColor,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(index, index + cleanQuery.length))
            }
            startIndex = index + cleanQuery.length
        }
    }
}

/**
 * Top section displaying pinned messages header bar and expandable pinned messages view.
 */
@Composable
private fun PinnedMessagesTopSection(
    pinnedMessages: List<Message>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onJumpToMessage: (String) -> Unit,
    onUnpinMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pinnedMessages.isEmpty() && !isExpanded) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pinned_messages_section"),
        color = Color(0xFFF8FAFC),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Pinned Messages Header Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("pinned_messages_header_strip"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pinned messages",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Pinned Messages",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE2E8F0)
                        ) {
                            Text(
                                text = "${pinnedMessages.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepBlue,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                                    .testTag("pinned_messages_count_badge")
                            )
                        }
                    }

                    if (pinnedMessages.isNotEmpty()) {
                        val latest = pinnedMessages.last()
                        Text(
                            text = "${latest.senderName.ifBlank { "User" }}: ${latest.text}",
                            fontSize = 12.sp,
                            color = TextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "No pinned messages",
                            fontSize = 12.sp,
                            color = TextLightGray
                        )
                    }
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("toggle_pinned_view_chevron")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse pinned messages" else "Expand pinned messages",
                        tint = DeepBlue
                    )
                }
            }

            // Expanded Pinned View
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("pinned_messages_expanded_panel")
                ) {
                    if (pinnedMessages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = null,
                                    tint = TextLightGray,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No pinned messages yet",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextGray
                                )
                                Text(
                                    text = "Long-press any message and tap the pin icon to pin it here.",
                                    fontSize = 12.sp,
                                    color = TextLightGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Important Messages in this Chat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextGray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pinnedMessages.forEach { msg ->
                                PinnedMessageCard(
                                    message = msg,
                                    onJump = { onJumpToMessage(msg.id) },
                                    onUnpin = { onUnpinMessage(msg.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card for an individual pinned message inside the expanded pinned view.
 */
@Composable
private fun PinnedMessageCard(
    message: Message,
    onJump: () -> Unit,
    onUnpin: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pinned_message_card_${message.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sender badge
                Text(
                    text = message.senderName.ifBlank { "User" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = DeepBlue,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = TextLightGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.text,
                fontSize = 13.sp,
                color = TextDark,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onUnpin,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("unpin_action_btn_${message.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Unpin",
                        tint = TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Unpin",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyanAccent.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clickable { onJump() }
                        .testTag("jump_to_pinned_btn_${message.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Jump to message",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepBlue
                        )
                    }
                }
            }
        }
    }
}

