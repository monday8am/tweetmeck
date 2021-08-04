package com.monday8am.tweetmeck.domain.auth

import com.monday8am.tweetmeck.data.remote.RequestToken
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.di.IoDispatcher
import com.monday8am.tweetmeck.domain.SuspendUseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class GetAuthUrlUseCase @Inject constructor(
    private val remoteClient: TwitterClient,
    @IoDispatcher defaultDispatcher: CoroutineDispatcher
) : SuspendUseCase<Unit, AuthUrlResponse>(defaultDispatcher) {

    override suspend fun execute(parameters: Unit): AuthUrlResponse {
        val token = remoteClient.getRequestToken()
        val url = remoteClient.getAuthUrl(token)
        return AuthUrlResponse(url, token)
    }
}

data class AuthUrlResponse(val url: String, val requestToken: RequestToken)
