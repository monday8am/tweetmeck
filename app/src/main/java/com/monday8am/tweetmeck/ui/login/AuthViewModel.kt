package com.monday8am.tweetmeck.ui.login

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class AuthViewModel : ViewModel(),
    SignInViewModelDelegate by GlobalContext.get().koin.get() {

    fun triggerLogIn() = viewModelScope.launch { startWebAuth() }

    fun setResult(url: Uri?, error: String?) = viewModelScope.launch { setWebAuthResult(url, error) }

    fun triggerLogOut() = viewModelScope.launch { logOut() }
}
