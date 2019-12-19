package com.monday8am.tweetmeck.data

import android.net.Uri
import androidx.lifecycle.LiveData
import com.monday8am.tweetmeck.data.local.SharedPreferencesService
import com.monday8am.tweetmeck.data.local.TwitterUserDao
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.data.remote.OAuthToken
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.util.map
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AuthRepository {
    fun loggedUserFlow(): LiveData<TwitterUser?>
    suspend fun getAuthUrl(): Result<String>
    suspend fun isLogged(): Boolean
    suspend fun login(resultUri: Uri): Result<Boolean>
    suspend fun logout()
}

class AuthRepositoryImpl(
    private val twitterClient: TwitterClient,
    private val twitterUserDao: TwitterUserDao,
    private val sharedPreferencesService: SharedPreferencesService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private val oauthVerifierConst = "oauth_verifier"
    private var requestToken: OAuthToken? = null

    override fun loggedUserFlow(): LiveData<TwitterUser?> {
        return twitterUserDao.loggedUser().map {
            if (it.isNotEmpty()) {
                it.first()
            } else {
                null
            }
        }
    }

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
                val response = twitterClient.getAccessToken(requestToken, verifier)
                sharedPreferencesService.saveAccessToken(response.accessToken)
                val userContent = twitterClient.getUser(response.userId)
                twitterUserDao.insert(userContent.copy(loggedUser = true))
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
