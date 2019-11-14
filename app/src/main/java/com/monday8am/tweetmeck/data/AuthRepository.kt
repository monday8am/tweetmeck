package com.monday8am.tweetmeck.data

import android.net.Uri
import com.monday8am.tweetmeck.data.local.SharedPreferencesService
import com.monday8am.tweetmeck.data.remote.OAuthToken
import com.monday8am.tweetmeck.data.remote.TwitterClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AuthRepository {
    suspend fun getAuthUrl(): String
    suspend fun isLogged(): Boolean
    suspend fun login(resultUri: Uri): Result<Boolean>
    suspend fun logout()
}

class DefaultAuthRepository(
    private val twitterClient: TwitterClient,
    private val sharedPreferencesService: SharedPreferencesService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private val oauthVerifierConst = "oauth_verifier"
    private var requestToken: OAuthToken? = null

    override suspend fun getAuthUrl(): String {
        return withContext(ioDispatcher) {
            val token = twitterClient.getRequestToken()
            requestToken = token
            twitterClient.getAuthUrl(token)
        }
    }

    override suspend fun isLogged(): Boolean {
        return withContext(ioDispatcher) {
            sharedPreferencesService.getAccessToken() != null
        }
    }

    override suspend fun login(resultUri: Uri): Result<Boolean> {
        val requestToken = requestToken ?: return Result.Error(exception = Exception("Invalid request token!"))
        val verifier = resultUri.getQueryParameter(oauthVerifierConst) ?: return Result.Error(exception = Exception("Invalid verifier token!"))
        withContext(ioDispatcher) {
            val accessToken = twitterClient.getAccessToken(requestToken, verifier)
            sharedPreferencesService.saveAccessToken(accessToken)
        }
        return Result.Success(true)
    }

    override suspend fun logout() {
        withContext(ioDispatcher) {
            sharedPreferencesService.deleteAccessToken()
        }
    }
}
