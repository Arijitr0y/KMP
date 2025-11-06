package org.example.project.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object EmailEntry : AuthUiState
    data class CodeEntry(val email: String) : AuthUiState
    data object Loading : AuthUiState
    data object Authed : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState: AuthUiState by mutableStateOf<AuthUiState>(AuthUiState.EmailEntry)
        private set

    private var statusJob: Job? = null

    init {
        statusJob = viewModelScope.launch {
            repo.sessionStatus.collectLatest { status ->
                // Be resilient across library versions: handle known cases, else treat as Loading
                uiState = when (status) {
                    is SessionStatus.Authenticated -> AuthUiState.Authed
                    is SessionStatus.NotAuthenticated -> AuthUiState.EmailEntry
                    is SessionStatus.Initializing -> AuthUiState.Loading
                    else -> AuthUiState.Loading
                }
            }
        }
    }

    fun requestOtp(email: String) = viewModelScope.launch {
        uiState = AuthUiState.Loading
        runCatching { repo.requestEmailOtp(email) }
            .onSuccess { uiState = AuthUiState.CodeEntry(email) }
            .onFailure { uiState = AuthUiState.Error(it.message ?: "Failed to request OTP") }
    }

    fun verifyOtp(email: String, code: String) = viewModelScope.launch {
        uiState = AuthUiState.Loading
        runCatching { repo.verifyEmailOtp(email, code) }
            .onFailure { uiState = AuthUiState.Error(it.message ?: "Invalid code") }
        // On success, session collector flips to Authed
    }

    fun signOut() = viewModelScope.launch {
        uiState = AuthUiState.Loading
        runCatching { repo.signOut() }
            .onFailure { uiState = AuthUiState.Error(it.message ?: "Sign-out failed") }
    }

    override fun onCleared() {
        statusJob?.cancel()
        super.onCleared()
    }
}
