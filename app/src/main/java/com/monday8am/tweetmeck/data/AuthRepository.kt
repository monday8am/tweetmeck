package com.monday8am.tweetmeck.data

import android.net.Uri
import com.github.scribejava.core.model.OAuth1RequestToken
import com.monday8am.tweetmeck.data.remote.TwitterAuthService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

interface AuthRepository {
    suspend fun getAuthUrl(): String
    suspend fun login(resultUri: Uri): Result<Boolean>
    suspend fun logout()
}

class DefaultAuthRepository(private val authService: TwitterAuthService,
                            private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO): AuthRepository {

    private val oauthTokenConst = "oauth_token"
    private val oauthVerifierConst = "oauth_verifier"
    private var requestToken: OAuth1RequestToken? = null

    override suspend fun getAuthUrl(): String {
        return withContext(ioDispatcher) {
            val token = authService.getRequestToken()
            requestToken = token
            authService.getAuthUrl(token)
        }
    }

    override suspend fun login(resultUri: Uri): Result<Boolean> {
        val token = requestToken ?: return Result.Error(exception = Exception("Invalid request token!"))
        val verifier = resultUri.getQueryParameter(oauthVerifierConst) ?: return Result.Error(exception = Exception("Invalid verifier token!"))
        Timber.d(verifier)
        withContext(ioDispatcher) {
            val session = authService.getAccessToken(token, verifier)
            // save session
        }
        return Result.Success(true)
    }

    override suspend fun logout() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}