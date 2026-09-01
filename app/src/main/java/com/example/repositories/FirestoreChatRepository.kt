package com.example.repositories

import android.util.Log
import com.example.models.Chat
import com.example.models.FirestoreSchema
import com.example.models.Message
import com.example.models.MessageStatus
import com.example.models.MessageType
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreChatRepository {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirestoreChatRepo", "FirebaseFirestore not initialized: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Subscribes to real-time message updates for a given chatId.
     */
    fun getMessagesStream(chatId: String): Flow<List<Message>> = callbackFlow {
        val db = firestore
        if (db == null) {
            // Provide fallback if Firestore is not initialized
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val messagesCollection = db.collection(FirestoreSchema.COLLECTION_CHATS)
            .document(chatId)
            .collection(FirestoreSchema.SUBCOLLECTION_MESSAGES)
            .orderBy(FirestoreSchema.MessageFields.FIELD_TIMESTAMP, Query.Direction.ASCENDING)

        val listenerRegistration = messagesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreChatRepo", "Error listening to messages for chat $chatId", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Message::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.w("FirestoreChatRepo", "Error deserializing message ${doc.id}: ${e.localizedMessage}")
                        null
                    }
                }
                trySend(messages)
            }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Subscribes to chat conversation details.
     */
    fun getChatStream(chatId: String): Flow<Chat?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val chatDoc = db.collection(FirestoreSchema.COLLECTION_CHATS).document(chatId)
        val listenerRegistration = chatDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreChatRepo", "Error listening to chat $chatId", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val chat = snapshot.toObject(Chat::class.java)?.copy(id = snapshot.id)
                trySend(chat)
            } else {
                trySend(null)
            }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Sends a new message to Firestore under `/chats/{chatId}/messages/{messageId}`
     * and updates conversation metadata in `/chats/{chatId}`.
     */
    suspend fun sendMessage(chatId: String, message: Message): Result<Message> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
        
        return try {
            val messageId = if (message.id.isNotBlank()) message.id else UUID.randomUUID().toString()
            val finalMessage = message.copy(
                id = messageId,
                chatId = chatId,
                timestamp = if (message.timestamp > 0) message.timestamp else System.currentTimeMillis(),
                status = MessageStatus.SENT
            )

            val chatRef = db.collection(FirestoreSchema.COLLECTION_CHATS).document(chatId)
            val messageRef = chatRef.collection(FirestoreSchema.SUBCOLLECTION_MESSAGES).document(messageId)

            // Write message document
            messageRef.set(finalMessage).await()

            // Update parent chat metadata
            val chatUpdates = mapOf(
                FirestoreSchema.ChatFields.FIELD_LAST_MESSAGE to finalMessage.text,
                FirestoreSchema.ChatFields.FIELD_LAST_MESSAGE_SENDER_ID to finalMessage.senderId,
                FirestoreSchema.ChatFields.FIELD_LAST_MESSAGE_SENDER_NAME to finalMessage.senderName,
                FirestoreSchema.ChatFields.FIELD_LAST_MESSAGE_TIMESTAMP to finalMessage.timestamp,
                FirestoreSchema.ChatFields.FIELD_LAST_MESSAGE_TYPE to finalMessage.type.name,
                FirestoreSchema.ChatFields.FIELD_LAST_MESSAGE_STATUS to finalMessage.status.name,
                FirestoreSchema.ChatFields.FIELD_UPDATED_AT to System.currentTimeMillis()
            )
            chatRef.set(chatUpdates, SetOptions.merge()).await()

            Result.success(finalMessage)
        } catch (e: Exception) {
            Log.e("FirestoreChatRepo", "Failed to send message to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Updates message delivery / read status for a single message.
     */
    suspend fun markMessageRead(chatId: String, messageId: String, userId: String, timestamp: Long = System.currentTimeMillis()) {
        val db = firestore ?: return
        try {
            val messageRef = db.collection(FirestoreSchema.COLLECTION_CHATS)
                .document(chatId)
                .collection(FirestoreSchema.SUBCOLLECTION_MESSAGES)
                .document(messageId)

            val update = mapOf(
                "${FirestoreSchema.MessageFields.FIELD_READ_BY}.$userId" to timestamp,
                FirestoreSchema.MessageFields.FIELD_STATUS to MessageStatus.READ.name
            )
            messageRef.update(update).await()
        } catch (e: Exception) {
            Log.w("FirestoreChatRepo", "Failed to mark message as read: ${e.localizedMessage}")
        }
    }

    /**
     * Marks all unread messages in a chat as READ by the current recipient.
     * Records the exact timestamp when the recipient opened the chat in the `readBy` map
     * and updates message statuses to READ in Firestore.
     */
    suspend fun markChatMessagesAsRead(
        chatId: String,
        userId: String,
        readTimestamp: Long = System.currentTimeMillis()
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (chatId.isBlank() || userId.isBlank()) {
            return Result.failure(IllegalArgumentException("chatId and userId cannot be blank"))
        }

        return try {
            val messagesRef = db.collection(FirestoreSchema.COLLECTION_CHATS)
                .document(chatId)
                .collection(FirestoreSchema.SUBCOLLECTION_MESSAGES)

            val snapshot = messagesRef.get().await()
            val batch = db.batch()
            var updatedCount = 0

            for (doc in snapshot.documents) {
                val senderId = doc.getString(FirestoreSchema.MessageFields.FIELD_SENDER_ID) ?: ""
                val currentStatus = doc.getString(FirestoreSchema.MessageFields.FIELD_STATUS) ?: ""
                @Suppress("UNCHECKED_CAST")
                val readByMap = doc.get(FirestoreSchema.MessageFields.FIELD_READ_BY) as? Map<String, Any> ?: emptyMap()

                // If sent by someone else and not yet read by this user
                if (senderId.isNotBlank() && senderId != userId && !readByMap.containsKey(userId)) {
                    val docRef = messagesRef.document(doc.id)
                    batch.update(
                        docRef,
                        mapOf(
                            "${FirestoreSchema.MessageFields.FIELD_READ_BY}.$userId" to readTimestamp,
                            FirestoreSchema.MessageFields.FIELD_STATUS to MessageStatus.READ.name
                        )
                    )
                    updatedCount++
                }
            }

            // Also reset unread counter on the parent chat document
            val chatRef = db.collection(FirestoreSchema.COLLECTION_CHATS).document(chatId)
            batch.update(
                chatRef,
                mapOf(
                    "${FirestoreSchema.ChatFields.FIELD_UNREAD_COUNTS}.$userId" to 0,
                    FirestoreSchema.ChatFields.FIELD_LAST_MESSAGE_STATUS to MessageStatus.READ.name,
                    FirestoreSchema.ChatFields.FIELD_UPDATED_AT to readTimestamp
                )
            )

            if (updatedCount > 0) {
                batch.commit().await()
                Log.d("FirestoreChatRepo", "Marked $updatedCount messages as read in chat $chatId at $readTimestamp")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreChatRepo", "Failed to mark chat messages as read: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    /**
     * Marks unread messages delivered to the recipient when received or loaded.
     */
    suspend fun markMessagesAsDelivered(
        chatId: String,
        userId: String,
        deliveredTimestamp: Long = System.currentTimeMillis()
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (chatId.isBlank() || userId.isBlank()) return Result.failure(IllegalArgumentException())

        return try {
            val messagesRef = db.collection(FirestoreSchema.COLLECTION_CHATS)
                .document(chatId)
                .collection(FirestoreSchema.SUBCOLLECTION_MESSAGES)

            val snapshot = messagesRef.get().await()
            val batch = db.batch()
            var count = 0

            for (doc in snapshot.documents) {
                val senderId = doc.getString(FirestoreSchema.MessageFields.FIELD_SENDER_ID) ?: ""
                val currentStatus = doc.getString(FirestoreSchema.MessageFields.FIELD_STATUS) ?: ""
                @Suppress("UNCHECKED_CAST")
                val deliveredMap = doc.get(FirestoreSchema.MessageFields.FIELD_DELIVERED_TO) as? Map<String, Any> ?: emptyMap()

                if (senderId.isNotBlank() && senderId != userId && !deliveredMap.containsKey(userId)) {
                    val docRef = messagesRef.document(doc.id)
                    val updates = mutableMapOf<String, Any>(
                        "${FirestoreSchema.MessageFields.FIELD_DELIVERED_TO}.$userId" to deliveredTimestamp
                    )
                    if (currentStatus == MessageStatus.SENT.name) {
                        updates[FirestoreSchema.MessageFields.FIELD_STATUS] = MessageStatus.DELIVERED.name
                    }
                    batch.update(docRef, updates)
                    count++
                }
            }

            if (count > 0) {
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreChatRepo", "Failed to mark messages as delivered: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    /**
     * Toggles an emoji reaction from a user on a specific message.
     * If the user has already reacted with this emoji, removes it.
     * Otherwise, adds the user ID to the emoji's list of reactors.
     */
    suspend fun toggleMessageReaction(
        chatId: String,
        messageId: String,
        emoji: String,
        userId: String
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (chatId.isBlank() || messageId.isBlank() || emoji.isBlank() || userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Parameters cannot be blank"))
        }

        return try {
            val messageRef = db.collection(FirestoreSchema.COLLECTION_CHATS)
                .document(chatId)
                .collection(FirestoreSchema.SUBCOLLECTION_MESSAGES)
                .document(messageId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(messageRef)
                if (snapshot.exists()) {
                    val currentMsg = snapshot.toObject(Message::class.java)
                    val reactionsMap = currentMsg?.reactions?.mapValues { it.value.toMutableList() }?.toMutableMap()
                        ?: mutableMapOf()

                    val reactors = reactionsMap[emoji] ?: mutableListOf()
                    if (reactors.contains(userId)) {
                        reactors.remove(userId)
                        if (reactors.isEmpty()) {
                            reactionsMap.remove(emoji)
                        } else {
                            reactionsMap[emoji] = reactors
                        }
                    } else {
                        reactors.add(userId)
                        reactionsMap[emoji] = reactors
                    }

                    transaction.update(
                        messageRef,
                        FirestoreSchema.MessageFields.FIELD_REACTIONS,
                        reactionsMap
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreChatRepo", "Failed to toggle reaction on message $messageId", e)
            Result.failure(e)
        }
    }

    /**
     * Updates the archived status for a user on a chat conversation.
     * When archived is true, the chat is hidden from the main list and kept in Archived.
     */
    suspend fun setChatArchived(chatId: String, userId: String, isArchived: Boolean): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (chatId.isBlank() || userId.isBlank()) {
            return Result.failure(IllegalArgumentException("chatId and userId cannot be blank"))
        }

        return try {
            val chatRef = db.collection(FirestoreSchema.COLLECTION_CHATS).document(chatId)
            val update = if (isArchived) {
                mapOf(FirestoreSchema.ChatFields.FIELD_IS_ARCHIVED_BY_USER_IDS to FieldValue.arrayUnion(userId))
            } else {
                mapOf(FirestoreSchema.ChatFields.FIELD_IS_ARCHIVED_BY_USER_IDS to FieldValue.arrayRemove(userId))
            }
            chatRef.update(update).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreChatRepo", "Failed to update archived state for chat $chatId: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    /**
     * Pins a message in Firestore: updates message document flags and adds messageId
     * to the parent chat document's pinnedMessageIds array.
     */
    suspend fun pinMessage(
        chatId: String,
        messageId: String,
        userId: String,
        timestamp: Long = System.currentTimeMillis()
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (chatId.isBlank() || messageId.isBlank() || userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Parameters cannot be blank"))
        }

        return try {
            val messageRef = db.collection(FirestoreSchema.COLLECTION_CHATS)
                .document(chatId)
                .collection(FirestoreSchema.SUBCOLLECTION_MESSAGES)
                .document(messageId)
            val chatRef = db.collection(FirestoreSchema.COLLECTION_CHATS).document(chatId)

            val batch = db.batch()
            batch.update(
                messageRef,
                mapOf(
                    FirestoreSchema.MessageFields.FIELD_IS_PINNED to true,
                    FirestoreSchema.MessageFields.FIELD_PINNED_BY_USER_ID to userId,
                    FirestoreSchema.MessageFields.FIELD_PINNED_AT to timestamp
                )
            )
            batch.update(
                chatRef,
                mapOf(
                    FirestoreSchema.ChatFields.FIELD_PINNED_MESSAGE_IDS to FieldValue.arrayUnion(messageId),
                    FirestoreSchema.ChatFields.FIELD_UPDATED_AT to timestamp
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreChatRepo", "Failed to pin message $messageId in chat $chatId", e)
            Result.failure(e)
        }
    }

    /**
     * Unpins a message in Firestore: updates message document flags and removes messageId
     * from the parent chat document's pinnedMessageIds array.
     */
    suspend fun unpinMessage(
        chatId: String,
        messageId: String,
        timestamp: Long = System.currentTimeMillis()
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (chatId.isBlank() || messageId.isBlank()) {
            return Result.failure(IllegalArgumentException("Parameters cannot be blank"))
        }

        return try {
            val messageRef = db.collection(FirestoreSchema.COLLECTION_CHATS)
                .document(chatId)
                .collection(FirestoreSchema.SUBCOLLECTION_MESSAGES)
                .document(messageId)
            val chatRef = db.collection(FirestoreSchema.COLLECTION_CHATS).document(chatId)

            val batch = db.batch()
            batch.update(
                messageRef,
                mapOf(
                    FirestoreSchema.MessageFields.FIELD_IS_PINNED to false,
                    FirestoreSchema.MessageFields.FIELD_PINNED_BY_USER_ID to null,
                    FirestoreSchema.MessageFields.FIELD_PINNED_AT to null
                )
            )
            batch.update(
                chatRef,
                mapOf(
                    FirestoreSchema.ChatFields.FIELD_PINNED_MESSAGE_IDS to FieldValue.arrayRemove(messageId),
                    FirestoreSchema.ChatFields.FIELD_UPDATED_AT to timestamp
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreChatRepo", "Failed to unpin message $messageId in chat $chatId", e)
            Result.failure(e)
        }
    }

    /**
     * Toggles the pinned status of a message.
     */
    suspend fun togglePinMessage(
        chatId: String,
        messageId: String,
        userId: String,
        isCurrentlyPinned: Boolean
    ): Result<Unit> {
        return if (isCurrentlyPinned) {
            unpinMessage(chatId, messageId)
        } else {
            pinMessage(chatId, messageId, userId)
        }
    }

    /**
     * Updates real-time typing status in Firestore for a user in a chat conversation.
     * When isTyping is true, adds the userId to typingUserIds array.
     * When isTyping is false, removes the userId from typingUserIds array.
     */
    suspend fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (chatId.isBlank() || userId.isBlank()) {
            return Result.failure(IllegalArgumentException("chatId and userId cannot be blank"))
        }

        return try {
            val chatRef = db.collection(FirestoreSchema.COLLECTION_CHATS).document(chatId)
            val update = if (isTyping) {
                mapOf(FirestoreSchema.ChatFields.FIELD_TYPING_USER_IDS to FieldValue.arrayUnion(userId))
            } else {
                mapOf(FirestoreSchema.ChatFields.FIELD_TYPING_USER_IDS to FieldValue.arrayRemove(userId))
            }
            chatRef.update(update).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreChatRepo", "Failed to update typing status for user $userId in chat $chatId: ${e.localizedMessage}")
            Result.failure(e)
        }
    }
}
