package com.monday8am.tweetmeck.ui.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.data.succeeded
import org.koin.core.KoinComponent
import org.koin.core.inject

sealed class AuthState {
    object NotLogged : AuthState()
    object Loading : AuthState()
    data class WaitingForUserCredentials(val url: String) : AuthState()
    data class Error(val errorMsg: String) : AuthState()
    object Logged : AuthState()
}

interface SignInViewModelDelegate {
    val currentUser: LiveData<TwitterUser?>
    val authState: LiveData<AuthState>
    suspend fun isLogged(): Boolean
    suspend fun triggerAuth()
    suspend fun setAuthResult(resultUri: Uri?, errorMsg: String? = null)
    suspend fun logOut()
}

class SignInViewModelDelegateImpl : SignInViewModelDelegate, KoinComponent {

    private val authRepository: AuthRepository by inject()
    private val _authState = MutableLiveData<AuthState>()

    override val currentUser: LiveData<TwitterUser?>
        get() = authRepository.loggedUserFlow()

    override val authState: LiveData<AuthState>
        get() = _authState

    override suspend fun isLogged(): Boolean {
        return authRepository.isLogged()
    }

    override suspend fun triggerAuth() {
        _authState.value = AuthState.Loading

        when (val response = authRepository.getAuthUrl()) {
            is Result.Success -> _authState.value = AuthState.WaitingForUserCredentials(response.data)
            is Result.Error -> _authState.value = AuthState.Error(response.exception.message ?: "Error getting auth URL")
            else -> _authState.value = AuthState.Error("Error getting auth URL")
        }
    }

    override suspend fun setAuthResult(resultUri: Uri?, errorMsg: String?) {
        when {
            resultUri != null -> {
                _authState.value = AuthState.Loading
                val result = authRepository.login(resultUri)
                if (result.succeeded) {
                    _authState.value = AuthState.Logged
                } else {
                    _authState.value = AuthState.Error(errorMsg = "Wrong result after login request")
                }
            }
            errorMsg != null -> _authState.value = AuthState.Error(errorMsg = errorMsg)
            else -> _authState.value = AuthState.NotLogged
        }
    }

    override suspend fun logOut() {
        authRepository.logout()
        _authState.value = AuthState.NotLogged
    }
}
