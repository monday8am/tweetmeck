package com.monday8am.tweetmeck.domain.lists

import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.mappers.ListToTwitterList
import com.monday8am.tweetmeck.data.mappers.asLambda
import com.monday8am.tweetmeck.data.mappers.mapWith
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.di.IoDispatcher
import com.monday8am.tweetmeck.domain.SuspendUseCase
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher

open class LoadListsFromRemoteUseCase @Inject constructor(
    private val remoteClient: TwitterClient,
    private val db: TwitterDatabase,
    @IoDispatcher defaultDispatcher: CoroutineDispatcher
) : SuspendUseCase<Pair<String, Session?>, Unit>(defaultDispatcher) {

    override suspend fun execute(parameters: Pair<String, Session?>) {
        val (screenName, session) = parameters
        val listsFromRemote = remoteClient.getLists(screenName, session).map { it.mapWith(
            ListToTwitterList().asLambda())
        }
        db.twitterListDao().updateAll(listsFromRemote)
    }
}
