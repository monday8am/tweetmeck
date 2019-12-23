package com.monday8am.tweetmeck.ui.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.succeeded
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed class AuthState {
    object NotLogged : AuthState()
    object Loading : AuthState()
    data class WaitingForUserCredentials(val url: String) : AuthState()
    data class Error(val errorMsg: String) : AuthState()
    object Logged : AuthState()
}

interface SignInViewModelDelegate {
    val currentSessionFlow: Flow<Session?>
    val authState: LiveData<Event<AuthState>>
    val isLogged: Boolean
    val lastSession: Session?
    suspend fun startWebAuth()
    suspend fun setWebAuthResult(resultUri: Uri?, errorMsg: String? = null)
    suspend fun logOut()
}

class SignInViewModelDelegateImpl(private val authRepository: AuthRepository) : SignInViewModelDelegate {

    private val _authState = MutableLiveData<AuthState>()
    override val authState: LiveData<Event<AuthState>>
        get() = _authState.map { Event(it) }

    override val isLogged: Boolean
        get() = _authState.value == AuthState.Logged

    override val currentSessionFlow: Flow<Session?>

    private var _lastSession: Session? = null
    override val lastSession: Session?
        get() = _lastSession

    init {
        currentSessionFlow = authRepository.session.map {
            _lastSession = it
            if (it == null) {
                _authState.value = AuthState.NotLogged
            } else {
                _authState.value = AuthState.Logged
            }
            it
        }
    }

    override suspend fun startWebAuth() {
        _authState.value = AuthState.Loading

        when (val response = authRepository.getAuthUrl()) {
            is Result.Success -> _authState.value = AuthState.WaitingForUserCredentials(response.data)
            is Result.Error -> _authState.value = AuthState.Error(response.exception.message ?: "Error getting auth URL")
            else -> _authState.value = AuthState.Error("Error getting auth URL")
        }
    }

    override suspend fun setWebAuthResult(resultUri: Uri?, errorMsg: String?) {
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
        _authState.value = AuthState.Loading
        authRepository.logout()
    }
}
