package com.monday8am.tweetmeck.data

import com.github.scribejava.core.model.OAuth1RequestToken
import com.monday8am.tweetmeck.data.remote.TwitterAuthService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AuthRepository {
    suspend fun getAuthUrl(): String
    suspend fun loginWithToken(requestToken: OAuth1RequestToken): Result<Boolean>
    suspend fun logout()
}

class DefaultAuthRepository(private val authService: TwitterAuthService,
                            private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): AuthRepository {

    override suspend fun getAuthUrl(): String {
        return withContext(ioDispatcher) {
            authService.getAuthUrl()
        }
    }

    override suspend fun loginWithToken(requestToken: OAuth1RequestToken): Result<Boolean> {
        // login remotely...
        // save session
        // return result
        withContext(ioDispatcher) {
            val session = authService.getAccessToken(requestToken, "")
        }
        return Result.Success(true)
    }

    override suspend fun logout() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}