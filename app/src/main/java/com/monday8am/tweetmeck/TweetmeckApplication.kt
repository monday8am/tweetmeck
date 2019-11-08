package com.monday8am.tweetmeck

import android.app.Application
import com.monday8am.tweetmeck.data.AuthRepository

class TweetmeckApplication: Application() {

    val authRepository: AuthRepository
        get() = ServiceLocator.provideAuthRepository()
}
