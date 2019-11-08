package com.monday8am.tweetmeck

import androidx.annotation.VisibleForTesting
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.DefaultAuthRepository
import com.monday8am.tweetmeck.data.remote.TwitterAuthServiceImpl

object ServiceLocator {

    @Volatile
    var authRepository: AuthRepository? = null
        @VisibleForTesting set

    fun provideAuthRepository(): AuthRepository {
        synchronized(this) {
            return authRepository ?: authRepository ?: createAuthRepository()
        }
    }

    private fun createAuthRepository(): AuthRepository {
        val twitterAuthService = TwitterAuthServiceImpl(BuildConfig.apiKey, BuildConfig.apiSecret)
        return DefaultAuthRepository(twitterAuthService)
    }
}
