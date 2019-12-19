package com.monday8am.tweetmeck.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel(),
    SignInViewModelDelegate by SignInViewModelDelegateImpl() {

    fun triggerLogout() = viewModelScope.launch { logOut() }
}
