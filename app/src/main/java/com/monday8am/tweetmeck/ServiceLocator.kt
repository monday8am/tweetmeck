package com.monday8am.tweetmeck

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.DefaultAuthRepository
import com.monday8am.tweetmeck.data.DefaultDataRepository
import com.monday8am.tweetmeck.data.local.LocalStorageService
import com.monday8am.tweetmeck.data.local.LocalStorageServiceImpl
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.data.remote.TwitterClientImpl

object ServiceLocator {

    private val lock = Any()

    @Volatile
    var authRepository: AuthRepository? = null
        @VisibleForTesting set

    @Volatile
    var dataRepository: DataRepository? = null
        @VisibleForTesting set

    private var localStorageService: LocalStorageService? = null

    fun provideAuthRepository(context: Context): AuthRepository {
        synchronized(this) {
            return authRepository ?: authRepository ?: createAuthRepository(context)
        }
    }

    fun provideDataRepository(context: Context): DataRepository {
        synchronized(this) {
            return dataRepository ?: dataRepository ?: createDataRepository(context)
        }
    }

    private fun createDataRepository(context: Context): DataRepository {
        return DefaultDataRepository(twitterClient = createTwitterClient(provideLocalStorageService(context)),
                                     localStorageService = provideLocalStorageService(context))
    }

    private fun createAuthRepository(context: Context): AuthRepository {
        return DefaultAuthRepository(twitterClient = createTwitterClient(provideLocalStorageService(context)),
                                     localStorageService = provideLocalStorageService(context))
    }

    private fun createTwitterClient(localStorage: LocalStorageService): TwitterClient {
        val token = localStorage.getAccessToken()
        return TwitterClientImpl(BuildConfig.apiKey,
                                  BuildConfig.apiSecret,
                                  token?.token ?: "",
                                  token?.secret ?: "",
                                  BuildConfig.callbackUrl)
    }

    private fun provideLocalStorageService(context: Context): LocalStorageService {
        synchronized(this) {
            return localStorageService ?: localStorageService ?: LocalStorageServiceImpl(context)
        }
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            // clean everything!
        }
    }
}
