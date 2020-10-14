package com.monday8am.tweetmeck.ui.delegates

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.data
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.remote.RequestToken
import com.monday8am.tweetmeck.data.succeeded
import com.monday8am.tweetmeck.domain.auth.GetAuthUrlUseCase
import com.monday8am.tweetmeck.domain.auth.ObserveLoggedSessionUseCase
import com.monday8am.tweetmeck.domain.auth.SignInUseCase
import com.monday8am.tweetmeck.domain.auth.SignOutUseCase
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.map
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed class AuthState {
    object NotLogged : AuthState()
    object Loading : AuthState()
    data class WaitingForUserCredentials(val url: String, val requestToken: RequestToken) : AuthState()
    data class Error(val errorMsg: String) : AuthState()
    object Logged : AuthState()
}

interface SignInViewModelDelegate {
    val observeSession: Flow<Session?>
    val authState: LiveData<Event<AuthState>>
    val isLogged: Boolean
    val lastSession: Session?
    suspend fun startWebAuth()
    suspend fun setWebAuthResult(resultUri: Uri?, token: RequestToken, errorMsg: String? = null)
    suspend fun logOut()
}

class SignInViewModelDelegateImpl @Inject constructor(
    observeCurrentSessionUseCase: ObserveLoggedSessionUseCase,
    private val getAuthUrlUseCase: GetAuthUrlUseCase,
    private val signInUseCase: SignInUseCase,
    private val signOutUseCase: SignOutUseCase
) :
    SignInViewModelDelegate {

    private val _authState = MutableLiveData<AuthState>()
    override val authState: LiveData<Event<AuthState>>
        get() = _authState.map { Event(it) }

    override val isLogged: Boolean
        get() = _authState.value == AuthState.Logged

    override val observeSession: Flow<Session?>

    private var _lastSession: Session? = null
    override val lastSession: Session?
        get() = _lastSession

    init {
        observeSession = observeCurrentSessionUseCase(Unit).map {
            if (it is Result.Success) {
                _lastSession = it.data
                _authState.value = if (_lastSession == null) {
                    AuthState.NotLogged
                } else {
                    AuthState.Logged
                }
            }
            it.data
        }
    }

    override suspend fun startWebAuth() {
        _authState.value = AuthState.Loading

        when (val response = getAuthUrlUseCase(Unit)) {
            is Result.Success -> _authState.value =
                AuthState.WaitingForUserCredentials(
                    response.data.url, response.data.requestToken
                )
            is Result.Error -> _authState.value =
                AuthState.Error(
                    response.exception.message ?: "Error getting auth URL"
                )
            else -> _authState.value =
                AuthState.Error("Error getting auth URL")
        }
    }

    override suspend fun setWebAuthResult(resultUri: Uri?, token: RequestToken, errorMsg: String?) {
        when {
            resultUri != null -> {
                _authState.value =
                    AuthState.Loading
                val result = signInUseCase(resultUri to token)
                if (result.succeeded) {
                    _authState.value = AuthState.Logged
                } else {
                    _authState.value = AuthState.Error(
                            errorMsg = "Wrong result after login request"
                        )
                }
            }
            errorMsg != null -> _authState.value =
                AuthState.Error(errorMsg = errorMsg)
            else -> _authState.value =
                AuthState.NotLogged
        }
    }

    override suspend fun logOut() {
        _authState.value = AuthState.Loading
        signOutUseCase(Unit)
    }
}
