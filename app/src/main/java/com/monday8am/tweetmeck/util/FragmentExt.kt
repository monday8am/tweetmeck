package com.monday8am.tweetmeck.util

import androidx.fragment.app.Fragment
import com.monday8am.tweetmeck.TweetmeckApplication
import com.monday8am.tweetmeck.ViewModelFactory

fun Fragment.getViewModelFactory(): ViewModelFactory {
    val authRepository = (requireContext().applicationContext as TweetmeckApplication).authRepository
    return ViewModelFactory(authRepository)
}
