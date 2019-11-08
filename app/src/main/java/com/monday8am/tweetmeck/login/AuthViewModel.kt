package com.monday8am.tweetmeck.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.scribejava.core.model.OAuth1RequestToken
import com.monday8am.tweetmeck.data.AuthRepository
import kotlinx.coroutines.launch


sealed class AuthState {
    object NotStarted : AuthState()
    object Starting : AuthState()
    data class Going(val url: String): AuthState()
    data class Error(val errorMsg: String): AuthState()
    object Success : AuthState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        _authState.value = AuthState.NotStarted
    }

    fun triggerAuth() {
        viewModelScope.launch {
            _authState.value = AuthState.Starting
            val url = authRepository.getAuthUrl()
            _authState.value = AuthState.Going(url)
        }
    }

    fun login(requestToken: OAuth1RequestToken) {
        viewModelScope.launch {
            val result = authRepository.loginWithToken(requestToken)
        }
    }

}
