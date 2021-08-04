package com.monday8am.tweetmeck.data.remote

import blue.starry.penicillin.PenicillinClient
import blue.starry.penicillin.core.session.ApiClient
import blue.starry.penicillin.core.session.config.account
import blue.starry.penicillin.core.session.config.application
import blue.starry.penicillin.core.session.config.token
import blue.starry.penicillin.endpoints.common.TweetMode
import blue.starry.penicillin.endpoints.favorites
import blue.starry.penicillin.endpoints.favorites.create
import blue.starry.penicillin.endpoints.favorites.destroy
import blue.starry.penicillin.endpoints.lists
import blue.starry.penicillin.endpoints.lists.list
import blue.starry.penicillin.endpoints.oauth
import blue.starry.penicillin.endpoints.oauth.AccessTokenResponse
import blue.starry.penicillin.endpoints.oauth.accessToken
import blue.starry.penicillin.endpoints.oauth.authenticateUrl
import blue.starry.penicillin.endpoints.oauth.requestToken
import blue.starry.penicillin.endpoints.search
import blue.starry.penicillin.endpoints.search.SearchResultType
import blue.starry.penicillin.endpoints.search.search
import blue.starry.penicillin.endpoints.statuses
import blue.starry.penicillin.endpoints.statuses.retweet
import blue.starry.penicillin.endpoints.statuses.unretweet
import blue.starry.penicillin.endpoints.timeline
import blue.starry.penicillin.endpoints.timeline.listTimeline
import blue.starry.penicillin.endpoints.users
import blue.starry.penicillin.endpoints.users.showByUserId
import blue.starry.penicillin.extensions.execute
import blue.starry.penicillin.models.Status
import blue.starry.penicillin.models.TwitterList
import blue.starry.penicillin.models.User
import com.monday8am.tweetmeck.data.models.Session
import io.ktor.http.Url
import timber.log.Timber

interface TwitterClient {
    // Auth
    suspend fun getRequestToken(): RequestToken
    suspend fun getAuthUrl(requestToken: RequestToken): String
    suspend fun getAccessToken(
        requestToken: RequestToken,
        oAuthVerifier: String
    ): AccessTokenResponse

    suspend fun likeTweet(id: Long, value: Boolean, session: Session): Status
    suspend fun retweetTweet(id: Long, value: Boolean, session: Session): Status
    suspend fun getLists(screenName: String, session: Session?): List<TwitterList>

    // Get content
    suspend fun getUser(id: Long): User
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
        val response =
            client.oauth.accessToken(requestToken.token, requestToken.secret, oAuthVerifier)
        // recreate client but this time logged.
        client = getClient(response.accessToken, response.accessTokenSecret)
        return response
    }

    override suspend fun getUser(id: Long): User {
        return client.users.showByUserId(id).execute().result
    }

    override suspend fun getLists(screenName: String, session: Session?): List<TwitterList> {
        if (session != null) {
            client = refreshClientCredentials(session.accessToken, session.accessTokenSecret)
        }
        val xx = client.lists.list(screenName).execute().results
        Timber.d("Lists: $xx")
        return xx
    }

    override suspend fun getListTimeline(
        listId: Long,
        sinceTweetId: Long?,
        maxTweetId: Long?,
        count: Int?
    ): List<Status> {
        val response = client.timeline.listTimeline(
            listId,
            count = count,
            sinceId = sinceTweetId,
            maxId = maxTweetId
        ).execute()
        return response.results
    }

    override suspend fun search(
        query: String,
        sinceTweetId: Long?,
        maxTweetId: Long?,
        count: Int?
    ): List<Status> {
        val response = client.search.search(
            query,
            count = count,
            sinceId = sinceTweetId,
            maxId = maxTweetId,
            tweetMode = TweetMode.Compat,
            resultType = SearchResultType.Default
        ).execute()
        return response.result.statuses
    }

    override suspend fun likeTweet(id: Long, value: Boolean, session: Session): Status {
        client = refreshClientCredentials(session.accessToken, session.accessTokenSecret)
        return if (value) {
            client.favorites.create(id).execute().result
        } else {
            client.favorites.destroy(id).execute().result
        }
    }

    override suspend fun retweetTweet(id: Long, value: Boolean, session: Session): Status {
        client = refreshClientCredentials(session.accessToken, session.accessTokenSecret)
        return if (value) {
            client.statuses.retweet(id).execute().result
        } else {
            client.statuses.unretweet(id).execute().result
        }
    }

    private fun refreshClientCredentials(
        accessToken: String,
        accessTokenSecret: String
    ): ApiClient {
        if (client.session.credentials.accessToken != accessToken ||
            client.session.credentials.accessTokenSecret != accessTokenSecret
        ) {
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
