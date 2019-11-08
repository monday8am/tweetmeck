package com.monday8am.tweetmeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.login.AuthViewModel

/**
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val authRepository: AuthRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(AuthViewModel::class.java) ->
                    AuthViewModel(authRepository)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
