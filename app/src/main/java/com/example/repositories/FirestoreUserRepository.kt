package com.example.repositories

import android.util.Log
import com.example.models.FirestoreSchema
import com.example.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirestoreUserRepo", "FirebaseFirestore not initialized: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Observes real-time user profile data at `/users/{userId}`.
     */
    fun getUserProfileStream(userId: String): Flow<User?> = callbackFlow {
        val db = firestore
        if (db == null || userId.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val userDoc = db.collection(FirestoreSchema.COLLECTION_USERS).document(userId)
        val listenerRegistration = userDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreUserRepo", "Error listening to user $userId", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = try {
                    snapshot.toObject(User::class.java)?.copy(id = snapshot.id)
                } catch (e: Exception) {
                    Log.w("FirestoreUserRepo", "Error deserializing user: ${e.localizedMessage}")
                    null
                }
                trySend(user)
            } else {
                trySend(null)
            }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Updates or creates the user profile document in Firestore at `/users/{userId}`.
     */
    suspend fun saveUserProfile(user: User): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
        if (user.id.isBlank()) return Result.failure(IllegalArgumentException("User ID cannot be blank"))

        return try {
            val userMap = mapOf(
                FirestoreSchema.UserFields.FIELD_ID to user.id,
                FirestoreSchema.UserFields.FIELD_DISPLAY_NAME to user.displayName,
                FirestoreSchema.UserFields.FIELD_EMAIL to user.email,
                FirestoreSchema.UserFields.FIELD_PHONE_NUMBER to user.phoneNumber,
                FirestoreSchema.UserFields.FIELD_PROFILE_PHOTO_URL to user.profilePhotoUrl,
                FirestoreSchema.UserFields.FIELD_STATUS to user.status,
                FirestoreSchema.UserFields.FIELD_BIO to user.bio,
                FirestoreSchema.UserFields.FIELD_IS_ONLINE to user.isOnline,
                FirestoreSchema.UserFields.FIELD_LAST_SEEN to System.currentTimeMillis()
            )

            db.collection(FirestoreSchema.COLLECTION_USERS)
                .document(user.id)
                .set(userMap, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "Failed to update profile for user ${user.id}", e)
            Result.failure(e)
        }
    }
}
