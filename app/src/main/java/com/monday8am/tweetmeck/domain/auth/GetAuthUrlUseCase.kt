package com.monday8am.tweetmeck.domain.auth

import com.monday8am.tweetmeck.data.remote.RequestToken
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.domain.SuspendUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

open class GetAuthUrlUseCase constructor(
    private val remoteClient: TwitterClient,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendUseCase<Unit, AuthUrlResponse>(defaultDispatcher) {

    override suspend fun execute(parameters: Unit): AuthUrlResponse {
        val token = remoteClient.getRequestToken()
        val url = remoteClient.getAuthUrl(token)
        return AuthUrlResponse(url, token)
    }
}

data class AuthUrlResponse(val url: String, val requestToken: RequestToken)
