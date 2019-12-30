package com.monday8am.tweetmeck.data

import android.net.Uri
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.mappers.UserToTwitterUser
import com.monday8am.tweetmeck.data.mappers.asLambda
import com.monday8am.tweetmeck.data.mappers.mapToSession
import com.monday8am.tweetmeck.data.mappers.mapWith
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.remote.OAuthToken
import com.monday8am.tweetmeck.data.remote.TwitterClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface AuthRepository {
    val session: Flow<Session?>
    suspend fun getAuthUrl(): Result<String>
    suspend fun login(resultUri: Uri): Result<Unit>
    suspend fun logout()
}

class AuthRepositoryImpl(
    private val twitterClient: TwitterClient,
    private val db: TwitterDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    private val oauthVerifierConst = "oauth_verifier"
    private var requestToken: OAuthToken? = null

    override val session: Flow<Session?>
        get() = db.sessionDao().currentSessionFlow().map { it.firstOrNull() }

    override suspend fun getAuthUrl(): Result<String> {
        return asResult {
            val token = twitterClient.getRequestToken()
            requestToken = token
            twitterClient.getAuthUrl(token)
        }
    }

    override suspend fun login(resultUri: Uri): Result<Unit> {
        val requestToken = requestToken ?: return Result.Error(exception = Exception("Invalid request token!"))
        val verifier = resultUri.getQueryParameter(oauthVerifierConst) ?: return Result.Error(exception = Exception("Invalid verifier token!"))
        return asResult {
            val session = twitterClient.getAccessToken(requestToken, verifier).mapToSession()
            val userContent = twitterClient.getUser(session.userId).mapWith(UserToTwitterUser().asLambda())
            cleanDatabase()
            db.twitterUserDao().insert(userContent)
            db.sessionDao().insert(session)
        }
    }

    override suspend fun logout() {
        cleanDatabase()
    }

    private suspend fun cleanDatabase() {
        withContext(ioDispatcher) {
            db.twitterUserDao().clear()
            db.twitterListDao().clear()
            db.tweetDao().clear()
            db.sessionDao().clear()
        }
    }

    private suspend fun <T> asResult(block: suspend() -> T): Result<T> {
        return withContext(ioDispatcher) {
            try {
                val result = block.invoke()
                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}
