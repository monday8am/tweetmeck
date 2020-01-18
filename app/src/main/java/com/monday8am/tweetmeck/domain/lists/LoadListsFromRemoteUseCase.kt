package com.monday8am.tweetmeck.domain.lists

import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.mappers.ListToTwitterList
import com.monday8am.tweetmeck.data.mappers.asLambda
import com.monday8am.tweetmeck.data.mappers.mapWith
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.domain.SuspendUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

open class LoadListsFromRemoteUseCase(
    private val remoteClient: TwitterClient,
    private val db: TwitterDatabase,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendUseCase<Pair<String, Session?>, Unit>(defaultDispatcher) {

    override suspend fun execute(parameters: Pair<String, Session?>) {
        val (screenName, session) = parameters
        val listsFromRemote = remoteClient.getLists(screenName, session).map { it.mapWith(
            ListToTwitterList().asLambda())
        }
        db.twitterListDao().updateAll(listsFromRemote)
    }
}
