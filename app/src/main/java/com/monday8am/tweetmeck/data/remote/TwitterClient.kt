package com.monday8am.tweetmeck.data.remote

import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.models.TwitterUser
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
import jp.nephy.penicillin.endpoints.users
import jp.nephy.penicillin.endpoints.users.showByUserId
import jp.nephy.penicillin.extensions.await
import jp.nephy.penicillin.models.User

interface TwitterClient {
    suspend fun getRequestToken(): RequestToken
    suspend fun getAuthUrl(requestToken: RequestToken): String
    suspend fun getAccessToken(requestToken: RequestToken, oAuthVerifier: String): AccessToken

    //suspend fun getUser(id: Long, withEntities: Boolean = false): TwitterUser
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

    /*
    override suspend fun getUser(id: Long, withEntities: Boolean): TwitterUser {
        val response = client.users.showByUserId(id, withEntities).await()
        val user: User = response.result
        return TwitterUser(123, "")
    } */

    override suspend fun getUserLists(): List<TwitterList> {
        val response = client.lists.list.await()
        return response.results.map { TwitterList.from(it) }
    }
}
