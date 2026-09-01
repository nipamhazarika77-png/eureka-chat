package com.example.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.User
import com.example.repositories.FirebaseAuthRepository
import com.example.repositories.FirestoreUserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ProfileEvent {
    data class ShowMessage(val message: String) : ProfileEvent()
}

class ProfileViewModel(
    private val userRepository: FirestoreUserRepository = FirestoreUserRepository(),
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val currentFirebaseUser = authRepository.currentUser
    val currentUserId: String = currentFirebaseUser?.uid ?: "user_me"
    val defaultEmail: String = currentFirebaseUser?.email ?: "nipamhazarika77@gmail.com"
    val defaultPhoneNumber: String = currentFirebaseUser?.phoneNumber ?: "+91 98765 43210"
    val defaultDisplayName: String = currentFirebaseUser?.displayName
        ?: if (defaultEmail.contains("@")) defaultEmail.substringBefore("@") else "User"

    private val _localUser = MutableStateFlow(
        User(
            id = currentUserId,
            displayName = defaultDisplayName,
            email = defaultEmail,
            phoneNumber = defaultPhoneNumber,
            status = "Available",
            bio = "Hey there! I am using EUREKA.",
            profilePhotoUrl = ""
        )
    )

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    val userState: StateFlow<User> = combine(
        userRepository.getUserProfileStream(currentUserId),
        _localUser
    ) { remoteUser, localUser ->
        if (remoteUser != null) {
            remoteUser
        } else {
            localUser
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _localUser.value
    )

    fun updateProfile(
        displayName: String,
        status: String,
        bio: String,
        profilePhotoUrl: String
    ) {
        val updatedUser = userState.value.copy(
            displayName = displayName.trim(),
            status = status.trim(),
            bio = bio.trim(),
            profilePhotoUrl = profilePhotoUrl.trim()
        )

        // Optimistically update local state
        _localUser.value = updatedUser

        viewModelScope.launch {
            _isSaving.value = true
            val result = userRepository.saveUserProfile(updatedUser)
            _isSaving.value = false

            if (result.isSuccess) {
                _events.emit(ProfileEvent.ShowMessage("Profile updated successfully"))
            } else {
                _events.emit(ProfileEvent.ShowMessage("Saved locally (Firestore sync pending)"))
            }
        }
    }
}
