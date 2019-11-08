package com.monday8am.tweetmeck

import android.app.Application
import com.monday8am.tweetmeck.data.AuthRepository
import timber.log.Timber

class TweetmeckApplication: Application() {

    val authRepository: AuthRepository
        get() = ServiceLocator.provideAuthRepository()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
