package com.monday8am.tweetmeck.data.remote

import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth1RequestToken
import com.github.scribejava.core.oauth.OAuth10aService
import timber.log.Timber

interface TwitterAuthService {
    suspend fun getRequestToken(): OAuth1RequestToken
    suspend fun getAuthUrl(token: OAuth1RequestToken): String
    suspend fun getAccessToken(requestToken: OAuth1RequestToken, oAuthVerifier: String): TwitterSession
}

data class TwitterAuthToken(val token: String, val secret: String)

data class TwitterSession(val userId: Long,
                          val userName: String,
                          val token: TwitterAuthToken)

class TwitterAuthServiceImpl(apiKey: String, apiSecret: String, callbackUrl: String = "") :
    TwitterAuthService {

    private var service: OAuth10aService = ServiceBuilder("Twitter")
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
    ): TwitterSession {
        val authToken = service.getAccessToken(requestToken, oAuthVerifier)
        Timber.d("access token info: ${authToken.token} ${authToken.tokenSecret}")
        return TwitterSession(0, "name", TwitterAuthToken(authToken.token, authToken.tokenSecret))
    }
}
