package com.monday8am.tweetmeck.ui.login

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.remote.RequestToken
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegate
import kotlinx.coroutines.launch

class AuthViewModel @ViewModelInject constructor(
    private val signInDelegate: SignInViewModelDelegate
) : ViewModel(), SignInViewModelDelegate by signInDelegate {

    fun triggerLogIn() = viewModelScope.launch { startWebAuth() }

    fun setResult(url: Uri?, token: RequestToken, error: String?) = viewModelScope.launch {
        setWebAuthResult(url, token, error)
    }

    fun triggerLogOut() = viewModelScope.launch { logOut() }
}
