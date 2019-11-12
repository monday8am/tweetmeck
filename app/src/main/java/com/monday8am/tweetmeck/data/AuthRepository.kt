package com.monday8am.tweetmeck.data

import android.net.Uri
import com.github.scribejava.core.model.OAuth1RequestToken
import com.monday8am.tweetmeck.data.local.LocalStorageService
import com.monday8am.tweetmeck.data.remote.TwitterAuthService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AuthRepository {
    suspend fun getAuthUrl(): String
    suspend fun isLogged(): Boolean
    suspend fun login(resultUri: Uri): Result<Boolean>
    suspend fun logout()
}

class DefaultAuthRepository(private val authService: TwitterAuthService,
                            private val localStorageService: LocalStorageService,
                            private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): AuthRepository {

    private val oauthVerifierConst = "oauth_verifier"
    private var requestToken: OAuth1RequestToken? = null

    override suspend fun getAuthUrl(): String {
        return withContext(ioDispatcher) {
            val token = authService.getRequestToken()
            requestToken = token
            authService.getAuthUrl(token)
        }
    }

    override suspend fun isLogged(): Boolean {
        return withContext(ioDispatcher) {
            localStorageService.getAccessToken() != null
        }
    }

    override suspend fun login(resultUri: Uri): Result<Boolean> {
        val requestToken = requestToken ?: return Result.Error(exception = Exception("Invalid request token!"))
        val verifier = resultUri.getQueryParameter(oauthVerifierConst) ?: return Result.Error(exception = Exception("Invalid verifier token!"))
        withContext(ioDispatcher) {
            val accessToken = authService.getAccessToken(requestToken, verifier)
            localStorageService.saveAccessToken(accessToken)
        }
        return Result.Success(true)
    }

    override suspend fun logout() {
        withContext(ioDispatcher) {
            localStorageService.deleteAccessToken()
        }
    }

}