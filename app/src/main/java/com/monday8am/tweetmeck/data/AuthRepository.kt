package com.monday8am.tweetmeck.data

import android.net.Uri
import com.monday8am.tweetmeck.data.local.SharedPreferencesService
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.mappers.mapToSession
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.remote.OAuthToken
import com.monday8am.tweetmeck.data.remote.TwitterClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface AuthRepository {
    val session: Flow<Session>
    suspend fun getAuthUrl(): Result<String>
    suspend fun isLogged(): Boolean
    suspend fun login(resultUri: Uri): Result<Boolean>
    suspend fun logout()
}

class AuthRepositoryImpl(
    private val twitterClient: TwitterClient,
    private val db: TwitterDatabase,
    private val sharedPreferencesService: SharedPreferencesService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private val oauthVerifierConst = "oauth_verifier"
    private var requestToken: OAuthToken? = null

    override suspend fun getAuthUrl(): Result<String> {
        return withContext(ioDispatcher) {
            try {
                val token = twitterClient.getRequestToken()
                requestToken = token
                Result.Success(twitterClient.getAuthUrl(token))
            } catch (e: Exception) {
                Result.Error(e)
            }
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
        return withContext(ioDispatcher) {
             try {
                val session = twitterClient.getAccessToken(requestToken, verifier).mapToSession()
                db.sessionDao().insert(session)
                val userContent = twitterClient.getUser(session.userId)
                 db.twitterUserDao().insert(userContent)
                Result.Success(true)
            } catch (e: Exception) {
                Result.Error(exception = e)
            }
        }
    }

    override suspend fun logout() {
        withContext(ioDispatcher) {
            sharedPreferencesService.deleteAccessToken()
        }
    }
}
