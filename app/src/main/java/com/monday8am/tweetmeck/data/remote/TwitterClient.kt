package com.monday8am.tweetmeck.data.remote

import com.monday8am.tweetmeck.BuildConfig.apiSecret
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.local.SharedPreferencesService
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.models.TwitterUser
import io.ktor.http.Url
import jp.nephy.penicillin.PenicillinClient
import jp.nephy.penicillin.core.session.ApiClient
import jp.nephy.penicillin.core.session.config.account
import jp.nephy.penicillin.core.session.config.application
import jp.nephy.penicillin.core.session.config.token
import jp.nephy.penicillin.endpoints.*
import jp.nephy.penicillin.endpoints.lists.list
import jp.nephy.penicillin.endpoints.oauth.accessToken
import jp.nephy.penicillin.endpoints.oauth.authenticateUrl
import jp.nephy.penicillin.endpoints.oauth.requestToken
import jp.nephy.penicillin.endpoints.timeline.listTimeline
import jp.nephy.penicillin.endpoints.users.showByUserId
import jp.nephy.penicillin.extensions.await

interface TwitterClient {
    suspend fun getRequestToken(): RequestToken
    suspend fun getAuthUrl(requestToken: RequestToken): String
    suspend fun getAccessToken(requestToken: RequestToken, oAuthVerifier: String): AccessToken
    suspend fun getUser(id: Long): TwitterUser
    suspend fun getLists(): List<TwitterList>
    suspend fun getListTimeline(listId: Long, sinceTweetId: Long? = null, count: Int = 30): Result<List<Tweet>>
}

data class OAuthToken(val token: String, val secret: String)

typealias RequestToken = OAuthToken

typealias AccessToken = OAuthToken

class TwitterClientImpl(
    private val apiKey: String,
    private val apiSecret: String,
    prefService: SharedPreferencesService,
    private val callbackUrl: String = ""
) :
    TwitterClient {

    private var client: ApiClient

    init {
        val savedToken = prefService.getAccessToken()
        client = getClient(savedToken?.token ?: "", savedToken?.secret ?: "")
    }

    override suspend fun getRequestToken(): RequestToken {
        val response = client.oauth.requestToken(callbackUrl = Url(callbackUrl).encodedPath)
        return RequestToken(response.requestToken, response.requestTokenSecret)
    }

    override suspend fun getAuthUrl(requestToken: RequestToken): String {
        return client.oauth.authenticateUrl(requestToken = requestToken.token).toString()
    }

    override suspend fun getAccessToken(
        requestToken: RequestToken,
        oAuthVerifier: String
    ): AccessToken {
        val response = client.oauth.accessToken(requestToken.token, requestToken.secret, oAuthVerifier)
        // recreate client but this time logged.
        client = getClient(response.accessToken, response.accessTokenSecret)
        return AccessToken(response.accessToken, response.accessTokenSecret)
    }

    override suspend fun getUser(id: Long): TwitterUser {
        val response = client.users.showByUserId(id).await()
        return TwitterUser.from(response.result)
    }

    override suspend fun getLists(): List<TwitterList> {
        val response = client.lists.list.await()
        return response.results.map { TwitterList.from(it) }
    }

    override suspend fun getListTimeline(listId: Long,
                                         sinceTweetId: Long?,
                                         count: Int): Result<List<Tweet>> {
        return try {
            val response = client.timeline.listTimeline(listId, count = count, sinceId = sinceTweetId).await()
            Result.Success(response.results.map { Tweet.from(it) })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun getClient(token: String, secret: String): ApiClient {
        return PenicillinClient {
            account {
                application(apiKey, apiSecret)
                token(token, secret)
            }
        }
    }
}
