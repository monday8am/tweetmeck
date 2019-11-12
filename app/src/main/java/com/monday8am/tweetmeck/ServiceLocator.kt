package com.monday8am.tweetmeck

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.DefaultAuthRepository
import com.monday8am.tweetmeck.data.local.LocalStorageServiceImpl
import com.monday8am.tweetmeck.data.remote.TwitterAuthServiceImpl

object ServiceLocator {

    @Volatile
    var authRepository: AuthRepository? = null
        @VisibleForTesting set

    fun provideAuthRepository(context: Context): AuthRepository {
        synchronized(this) {
            return authRepository ?: authRepository ?: createAuthRepository(context)
        }
    }

    private fun createAuthRepository(context: Context): AuthRepository {
        val twitterAuthService = TwitterAuthServiceImpl(BuildConfig.apiKey,
                                                        BuildConfig.apiSecret,
                                                        BuildConfig.callbackUrl)
        val localStorageService = LocalStorageServiceImpl(context)

        return DefaultAuthRepository(authService = twitterAuthService,
                                     localStorageService = localStorageService)
    }
}
