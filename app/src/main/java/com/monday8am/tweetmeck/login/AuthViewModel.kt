package com.monday8am.tweetmeck.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


sealed class AuthState {
    object NotStarted : AuthState()
    object Starting : AuthState()
    data class Going(val url: String): AuthState()
    data class Error(val errorMsg: String): AuthState()
    object Success : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        _authState.value = AuthState.NotStarted
    }

    fun triggerAuth() {

    }

}
