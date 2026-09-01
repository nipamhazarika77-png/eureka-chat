package com.example.models

/**
 * Defines the Firebase Firestore database schema, collection hierarchies, document paths,
 * field name constants, and query strategies for the EUREKA messaging platform.
 *
 * Firestore Document Hierarchy:
 *
 *  /users/{userId}
 *      - User profile document (User data model)
 *
 *  /chats/{chatId}
 *      - Chat metadata & conversation document (Chat data model)
 *      - Subcollection:
 *          /chats/{chatId}/messages/{messageId}
 *              - Individual message history document (Message data model)
 *
 *  /typing_indicators/{chatId}_{userId}
 *      - Ephemeral typing status records
 */
object FirestoreSchema {

    // Root Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CHATS = "chats"
    const val COLLECTION_TYPING = "typing_indicators"
    
    // Subcollections
    const val SUBCOLLECTION_MESSAGES = "messages"

    /**
     * User Document Field Names
     */
    object UserFields {
        const val FIELD_ID = "id"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_EMAIL = "email"
        const val FIELD_PHONE_NUMBER = "phoneNumber"
        const val FIELD_PROFILE_PHOTO_URL = "profilePhotoUrl"
        const val FIELD_IS_ONLINE = "isOnline"
        const val FIELD_LAST_SEEN = "lastSeen"
        const val FIELD_STATUS = "status"
        const val FIELD_BIO = "bio"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_FCM_TOKEN = "fcmToken"
    }

    /**
     * Chat Document Field Names (/chats/{chatId})
     */
    object ChatFields {
        const val FIELD_ID = "id"
        const val FIELD_TYPE = "type"
        const val FIELD_NAME = "name"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_PHOTO_URL = "photoUrl"
        const val FIELD_PARTICIPANT_IDS = "participantIds"
        const val FIELD_ADMIN_IDS = "adminIds"
        const val FIELD_CREATED_BY = "createdBy"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"
        
        // Last Message Metadata
        const val FIELD_LAST_MESSAGE = "lastMessage"
        const val FIELD_LAST_MESSAGE_SENDER_ID = "lastMessageSenderId"
        const val FIELD_LAST_MESSAGE_SENDER_NAME = "lastMessageSenderName"
        const val FIELD_LAST_MESSAGE_TIMESTAMP = "lastMessageTimestamp"
        const val FIELD_LAST_MESSAGE_TYPE = "lastMessageType"
        const val FIELD_LAST_MESSAGE_STATUS = "lastMessageStatus"
        
        // User Preferences & State
        const val FIELD_UNREAD_COUNTS = "unreadCounts"
        const val FIELD_PINNED_BY_USER_IDS = "pinnedByUserIds"
        const val FIELD_PINNED_MESSAGE_IDS = "pinnedMessageIds"
        const val FIELD_MUTED_BY_USER_IDS = "mutedByUserIds"
        const val FIELD_TYPING_USER_IDS = "typingUserIds"
        const val FIELD_IS_ARCHIVED_BY_USER_IDS = "isArchivedByUserIds"
    }

    /**
     * Message Document Field Names (/chats/{chatId}/messages/{messageId})
     */
    object MessageFields {
        const val FIELD_ID = "id"
        const val FIELD_CHAT_ID = "chatId"
        const val FIELD_SENDER_ID = "senderId"
        const val FIELD_SENDER_NAME = "senderName"
        const val FIELD_SENDER_PHOTO_URL = "senderPhotoUrl"
        const val FIELD_TEXT = "text"
        const val FIELD_TIMESTAMP = "timestamp"
        const val FIELD_SERVER_TIMESTAMP = "serverTimestamp"
        const val FIELD_STATUS = "status"
        const val FIELD_TYPE = "type"
        
        // Media Attachment Fields
        const val FIELD_MEDIA_URL = "mediaUrl"
        const val FIELD_MEDIA_THUMBNAIL_URL = "mediaThumbnailUrl"
        const val FIELD_MEDIA_MIME_TYPE = "mediaMimeType"
        const val FIELD_MEDIA_SIZE = "mediaSize"
        const val FIELD_MEDIA_DURATION = "mediaDuration"
        const val FIELD_MEDIA_NAME = "mediaName"
        
        // Threading & Replies
        const val FIELD_REPLY_TO_MESSAGE_ID = "replyToMessageId"
        const val FIELD_REPLY_TO_SENDER_NAME = "replyToSenderName"
        const val FIELD_REPLY_TO_MESSAGE_SNIPPET = "replyToMessageSnippet"
        
        // Interactive / Receipts
        const val FIELD_REACTIONS = "reactions"
        const val FIELD_READ_BY = "readBy"
        const val FIELD_DELIVERED_TO = "deliveredTo"
        
        // Status Flags
        const val FIELD_IS_EDITED = "isEdited"
        const val FIELD_EDITED_TIMESTAMP = "editedTimestamp"
        const val FIELD_IS_DELETED = "isDeleted"
        const val FIELD_DELETED_FOR_USER_IDS = "deletedForUserIds"
        const val FIELD_STARRED_BY_USER_IDS = "starredByUserIds"
        const val FIELD_IS_PINNED = "isPinned"
        const val FIELD_PINNED_BY_USER_ID = "pinnedByUserId"
        const val FIELD_PINNED_AT = "pinnedAt"
    }

    /**
     * Helper functions to construct Firestore document and collection paths
     */
    fun userPath(userId: String): String = "$COLLECTION_USERS/$userId"
    fun chatPath(chatId: String): String = "$COLLECTION_CHATS/$chatId"
    fun messagesCollectionPath(chatId: String): String = "$COLLECTION_CHATS/$chatId/$SUBCOLLECTION_MESSAGES"
    fun messagePath(chatId: String, messageId: String): String = "$COLLECTION_CHATS/$chatId/$SUBCOLLECTION_MESSAGES/$messageId"
}
