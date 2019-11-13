package com.monday8am.tweetmeck.data.remote

import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth1RequestToken
import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.oauth.OAuth10aService
import okhttp3.Request

interface TwitterAuthService {
    suspend fun getRequestToken(): OAuth1RequestToken
    suspend fun getAuthUrl(token: OAuth1RequestToken): String
    suspend fun getAccessToken(requestToken: OAuth1RequestToken, oAuthVerifier: String): TwitterAccessToken
    fun signRequest(request: OAuthRequest): Request
}

data class TwitterAccessToken(val token: String, val secret: String)

data class TwitterSession(val userId: Long,
                          val userName: String,
                          val token: TwitterAccessToken
)

class TwitterAuthServiceImpl(apiKey: String, apiSecret: String, callbackUrl: String = "") :
    TwitterAuthService {

    private var service: OAuth10aService = ServiceBuilder("TwitterAuthService")
                                                .apiKey(apiKey)
                                                .apiSecret(apiSecret)
                                                .callback(callbackUrl)
                                                .build(TwitterApi.instance())

    override suspend fun getRequestToken(): OAuth1RequestToken {
        return service.requestToken
    }

    override suspend fun getAuthUrl(token: OAuth1RequestToken): String {
        return service.getAuthorizationUrl(token)
    }

    override suspend fun getAccessToken(
        requestToken: OAuth1RequestToken,
        oAuthVerifier: String
    ): TwitterAccessToken {
        val authToken = service.getAccessToken(requestToken, oAuthVerifier)
        return TwitterAccessToken(authToken.token, authToken.tokenSecret)
    }

    override fun signRequest(request: OAuthRequest): Request {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
