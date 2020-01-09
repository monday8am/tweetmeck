package com.monday8am.tweetmeck.data.remote

import com.monday8am.tweetmeck.data.models.Session
import io.ktor.http.Url
import jp.nephy.penicillin.PenicillinClient
import jp.nephy.penicillin.core.session.ApiClient
import jp.nephy.penicillin.core.session.config.account
import jp.nephy.penicillin.core.session.config.application
import jp.nephy.penicillin.core.session.config.token
import jp.nephy.penicillin.endpoints.*
import jp.nephy.penicillin.endpoints.favorites.create
import jp.nephy.penicillin.endpoints.favorites.destroy
import jp.nephy.penicillin.endpoints.lists.list
import jp.nephy.penicillin.endpoints.oauth.AccessTokenResponse
import jp.nephy.penicillin.endpoints.oauth.accessToken
import jp.nephy.penicillin.endpoints.oauth.authenticateUrl
import jp.nephy.penicillin.endpoints.oauth.requestToken
import jp.nephy.penicillin.endpoints.search.search
import jp.nephy.penicillin.endpoints.statuses.retweet
import jp.nephy.penicillin.endpoints.statuses.unretweet
import jp.nephy.penicillin.endpoints.timeline.listTimeline
import jp.nephy.penicillin.endpoints.users.showByUserId
import jp.nephy.penicillin.extensions.await
import jp.nephy.penicillin.models.Status
import jp.nephy.penicillin.models.TwitterList
import jp.nephy.penicillin.models.User

interface TwitterClient {
    // Auth
    suspend fun getRequestToken(): RequestToken
    suspend fun getAuthUrl(requestToken: RequestToken): String
    suspend fun getAccessToken(requestToken: RequestToken, oAuthVerifier: String): AccessTokenResponse

    // Logged operations
    suspend fun likeTweet(id: Long, value: Boolean, session: Session): Status
    suspend fun retweetTweet(id: Long, value: Boolean, session: Session): Status
    suspend fun getLoggedUserLists(session: Session): List<TwitterList>

    // Get content
    suspend fun getUser(id: Long): User
    suspend fun getLists(screenName: String): List<TwitterList>
    suspend fun getListTimeline(
        listId: Long,
        sinceTweetId: Long? = null,
        maxTweetId: Long? = null,
        count: Int?
    ): List<Status>
    suspend fun search(
        query: String,
        sinceTweetId: Long? = null,
        maxTweetId: Long? = null,
        count: Int? = null
    ): List<Status>
}

data class OAuthToken(val token: String, val secret: String)

typealias RequestToken = OAuthToken

typealias AccessToken = OAuthToken

class TwitterClientImpl(
    private val apiKey: String,
    private val apiSecret: String,
    private val accessToken: String,
    private val accessTokenSecret: String,
    private val callbackUrl: String = ""
) :
    TwitterClient {

    private var client: ApiClient = getClient(accessToken, accessTokenSecret)

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
    ): AccessTokenResponse {
        val response = client.oauth.accessToken(requestToken.token, requestToken.secret, oAuthVerifier)
        // recreate client but this time logged.
        client = getClient(response.accessToken, response.accessTokenSecret)
        return response
    }

    override suspend fun getUser(id: Long): User {
        return client.users.showByUserId(id).await().result
    }

    override suspend fun getLoggedUserLists(session: Session): List<TwitterList> {
        client = refreshClientCredentials(session.accessToken, session.accessTokenSecret)
        return client.lists.list().await().results
    }

    override suspend fun getLists(screenName: String): List<TwitterList> {
        return client.lists.list(screenName).await().results
    }

    override suspend fun getListTimeline(
        listId: Long,
        sinceTweetId: Long?,
        maxTweetId: Long?,
        count: Int?
    ): List<Status> {
        val response = client.timeline.listTimeline(listId,
            count = count,
            sinceId = sinceTweetId,
            maxId = maxTweetId).await()
        return response.results
    }

    override suspend fun search(
        query: String,
        sinceTweetId: Long?,
        maxTweetId: Long?,
        count: Int?
    ): List<Status> {
        val response = client.search.search(query,
            count = count,
            sinceId = sinceTweetId,
            maxId = maxTweetId).await()
        return response.result.statuses
    }

    override suspend fun likeTweet(id: Long, value: Boolean, session: Session): Status {
        client = refreshClientCredentials(session.accessToken, session.accessTokenSecret)
        return if (value) {
            client.favorites.create(id).await().result
        } else {
            client.favorites.destroy(id).await().result
        }
    }

    override suspend fun retweetTweet(id: Long, value: Boolean, session: Session): Status {
        client = refreshClientCredentials(session.accessToken, session.accessTokenSecret)
        return if (value) {
            client.statuses.retweet(id).await().result
        } else {
            client.statuses.unretweet(id).await().result
        }
    }

    private fun refreshClientCredentials(accessToken: String, accessTokenSecret: String): ApiClient {
        if (client.session.credentials.accessToken != accessToken ||
                client.session.credentials.accessTokenSecret != accessTokenSecret) {
            return getClient(accessToken, accessTokenSecret)
        }
        return client
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
