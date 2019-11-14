package com.monday8am.tweetmeck.data.remote

import com.monday8am.tweetmeck.data.models.TwitterList
import io.ktor.http.Url
import jp.nephy.penicillin.PenicillinClient
import jp.nephy.penicillin.core.session.config.account
import jp.nephy.penicillin.core.session.config.application
import jp.nephy.penicillin.core.session.config.token
import jp.nephy.penicillin.endpoints.lists
import jp.nephy.penicillin.endpoints.lists.list
import jp.nephy.penicillin.endpoints.oauth
import jp.nephy.penicillin.endpoints.oauth.accessToken
import jp.nephy.penicillin.endpoints.oauth.authenticateUrl
import jp.nephy.penicillin.endpoints.oauth.requestToken
import jp.nephy.penicillin.extensions.await

interface TwitterClient {
    suspend fun getRequestToken(): RequestToken
    suspend fun getAuthUrl(requestToken: RequestToken): String
    suspend fun getAccessToken(requestToken: RequestToken, oAuthVerifier: String): AccessToken
    suspend fun getUserLists(): List<TwitterList>
}

data class OAuthToken(val token: String, val secret: String)

typealias RequestToken = OAuthToken

typealias AccessToken = OAuthToken

class TwitterClientImpl(
    apiKey: String,
    apiSecret: String,
    accessToken: String = "",
    accessTokenSecret: String = "",
    private val callbackUrl: String = ""
) :
    TwitterClient {

    private var client = PenicillinClient {
        account {
            application(apiKey, apiSecret)
            token(accessToken, accessTokenSecret)
        }
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
        return AccessToken(response.accessToken, response.accessTokenSecret)
    }

    override suspend fun getUserLists(): List<TwitterList> {
        val twitterLists = client.lists.list.await()
        return twitterLists.results.map { TwitterList.from(it) }
    }
}
