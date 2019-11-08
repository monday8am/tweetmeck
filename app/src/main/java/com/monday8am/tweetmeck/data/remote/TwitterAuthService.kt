package com.monday8am.tweetmeck.data.remote

import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth1RequestToken
import com.github.scribejava.core.oauth.OAuth10aService

interface TwitterAuthService {
    suspend fun getAuthUrl(): String
    suspend fun getAccessToken(requestToken: OAuth1RequestToken, oAuthVerifier: String): TwitterSession
}

data class TwitterAuthToken(val token: String, val secret: String)

data class TwitterSession(val userId: Long,
                          val userName: String,
                          val token: TwitterAuthToken)

class TwitterAuthServiceImpl(apiKey: String, apiSecret: String) :
    TwitterAuthService {

    private var service: OAuth10aService = ServiceBuilder("Twitter")
                                                .apiKey(apiKey)
                                                .apiSecret(apiSecret)
                                                .build(TwitterApi.instance())

    override suspend fun getAuthUrl(): String {
        val token = service.requestToken
        return service.getAuthorizationUrl(token)
    }

    override suspend fun getAccessToken(
        requestToken: OAuth1RequestToken,
        oAuthVerifier: String
    ): TwitterSession {
        val authToken = service.getAccessToken(requestToken, oAuthVerifier)
        return TwitterSession(0, "name", TwitterAuthToken("token", "secret"))
    }
}
