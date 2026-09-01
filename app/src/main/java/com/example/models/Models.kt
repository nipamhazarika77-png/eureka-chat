package com.example.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

/**
 * Represents a user profile in Firestore and the app.
 * Stored at `/users/{userId}`.
 */
@IgnoreExtraProperties
@Serializable
data class User(
    @DocumentId
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePhotoUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val status: String = "Available",
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val fcmToken: String = ""
)

/**
 * Message status progression lifecycle.
 */
enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

/**
 * Types of content supported within messages.
 */
enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    VOICE,
    DOCUMENT,
    LOCATION,
    CONTACT,
    SYSTEM,
    POLL
}

/**
 * Chat conversation types.
 */
enum class ChatType {
    ONE_TO_ONE,
    GROUP,
    AI,
    CHANNEL
}

/**
 * Represents a single message document in Firestore.
 * Stored at `/chats/{chatId}/messages/{messageId}`.
 */
@IgnoreExtraProperties
@Serializable
data class Message(
    @DocumentId
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    @ServerTimestamp
    @kotlinx.serialization.Transient
    val serverTimestamp: Date? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val type: MessageType = MessageType.TEXT,
    
    // Media & Attachment Details
    val mediaUrl: String? = null,
    val mediaThumbnailUrl: String? = null,
    val mediaMimeType: String? = null,
    val mediaSize: Long = 0L,
    val mediaDuration: Long = 0L,
    val mediaName: String? = null,
    
    // Reply & Threading
    val replyToMessageId: String? = null,
    val replyToSenderName: String? = null,
    val replyToMessageSnippet: String? = null,
    
    // Reactions: Emoji -> List of user IDs
    val reactions: Map<String, List<String>> = emptyMap(),
    
    // Delivery & Read Receipts: User ID -> Timestamp
    val readBy: Map<String, Long> = emptyMap(),
    val deliveredTo: Map<String, Long> = emptyMap(),
    
    // Status flags
    val isEdited: Boolean = false,
    val editedTimestamp: Long? = null,
    val isDeleted: Boolean = false,
    val deletedForUserIds: List<String> = emptyList(),
    val starredByUserIds: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val pinnedByUserId: String? = null,
    val pinnedAt: Long? = null
)

/**
 * Represents a chat conversation metadata document in Firestore.
 * Stored at `/chats/{chatId}`.
 */
@IgnoreExtraProperties
@Serializable
data class Chat(
    @DocumentId
    val id: String = "",
    val type: ChatType = ChatType.ONE_TO_ONE,
    val name: String = "",
    val description: String = "",
    val photoUrl: String = "",
    
    // Participants & Permissions
    val participantIds: List<String> = emptyList(),
    val adminIds: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Last Message Preview for Chat List
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageSenderName: String = "",
    val lastMessageTimestamp: Long = 0L,
    val lastMessageType: MessageType = MessageType.TEXT,
    val lastMessageStatus: MessageStatus = MessageStatus.SENT,
    
    // Local / Member-specific metadata
    val unreadCount: Int = 0,
    val unreadCounts: Map<String, Int> = emptyMap(), // userId -> count
    val pinnedByUserIds: List<String> = emptyList(),
    val pinnedMessageIds: List<String> = emptyList(),
    val mutedByUserIds: List<String> = emptyList(),
    val typingUserIds: List<String> = emptyList(),
    val isArchivedByUserIds: List<String> = emptyList()
)
