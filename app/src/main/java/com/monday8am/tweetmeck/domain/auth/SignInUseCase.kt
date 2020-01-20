package com.monday8am.tweetmeck.domain.auth

import android.net.Uri
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.mappers.UserToTwitterUser
import com.monday8am.tweetmeck.data.mappers.asLambda
import com.monday8am.tweetmeck.data.mappers.mapToSession
import com.monday8am.tweetmeck.data.mappers.mapWith
import com.monday8am.tweetmeck.data.remote.RequestToken
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.domain.SuspendUseCase
import com.monday8am.tweetmeck.domain.oauthVerifierConst
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

open class SignInUseCase constructor(
    private val remoteClient: TwitterClient,
    private val db: TwitterDatabase,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendUseCase<Pair<Uri, RequestToken>, Unit>(defaultDispatcher) {

    override suspend fun execute(parameters: Pair<Uri, RequestToken>) {
        val (resultUri, requestToken) = parameters

        val verifier = resultUri.getQueryParameter(oauthVerifierConst) ?: throw Exception("Invalid verifier token!")
        val session = remoteClient.getAccessToken(requestToken, verifier).mapToSession()
        val userContent = remoteClient.getUser(session.userId).mapWith(UserToTwitterUser().asLambda())

        db.clearAllTables()

        db.twitterUserDao().insert(userContent)
        db.sessionDao().insert(session)
    }
}
