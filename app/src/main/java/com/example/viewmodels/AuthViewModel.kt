package com.example.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repositories.FirebaseAuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val authRepository = FirebaseAuthRepository()

    private val _authState = MutableStateFlow<AuthState>(
        if (authRepository.currentUser != null) AuthState.Authenticated else AuthState.Idle
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var storedVerificationId: String? = null
    
    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    fun sendOtp(rawPhoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading
        val phoneNumber = rawPhoneNumber.trim().let { num ->
            val clean = num.replace(Regex("[^0-9+]"), "")
            if (clean.startsWith("+")) clean else "+$clean"
        }
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch {
                    val result = authRepository.signInWithCredential(credential)
                    if (result.isSuccess) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
                    }
                }
            }
            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Verification failed")
            }
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                storedVerificationId = verificationId
                _otpSent.value = true
                _authState.value = AuthState.Idle
            }
        }
        authRepository.sendOtp(phoneNumber, activity, callbacks)
    }

    fun verifyOtp(code: String) {
        val verificationId = storedVerificationId ?: return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithOtp(verificationId, code)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun resetOtpState() {
        _otpSent.value = false
        _authState.value = AuthState.Idle
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithEmailAndPassword(email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.registerWithEmailAndPassword(email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInAnonymously()
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithGoogleCredential(idToken)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Idle
    }
}
