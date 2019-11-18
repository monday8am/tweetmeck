package com.monday8am.tweetmeck.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.succeeded
import kotlinx.coroutines.launch

sealed class AuthState {
    object NotLogged    : AuthState()
    object Loading : AuthState()
    data class WaitingForUserCredentials(val url: String) : AuthState()
    data class Error(val errorMsg: String) : AuthState()
    object Logged : AuthState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        _authState.value = AuthState.Loading
        isLogged()
    }

    fun triggerAuth() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val url = authRepository.getAuthUrl()
            _authState.value = AuthState.WaitingForUserCredentials(url)
        }
    }

    fun setAuthResult(resultUri: Uri?, errorMsg: String? = null) {
        viewModelScope.launch {
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
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.NotLogged
        }
    }

    private fun isLogged() {
        viewModelScope.launch {
            _authState.value = if (authRepository.isLogged()) {
                AuthState.Logged
            } else {
                AuthState.NotLogged
            }
        }
    }
}
