package com.monday8am.tweetmeck.domain.timeline

import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.mappers.StatusToTweet
import com.monday8am.tweetmeck.data.mappers.StatusToTwitterUser
import com.monday8am.tweetmeck.data.mappers.asLambda
import com.monday8am.tweetmeck.data.mappers.mapWith
import com.monday8am.tweetmeck.data.models.ModelId
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.di.IoDispatcher
import com.monday8am.tweetmeck.domain.SuspendUseCase
import com.monday8am.tweetmeck.domain.pageSize
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class RefreshListTimelineUseCase @Inject constructor(
    private val remoteClient: TwitterClient,
    private val db: TwitterDatabase,
    @IoDispatcher defaultDispatcher: CoroutineDispatcher
) : SuspendUseCase<ModelId, Unit>(defaultDispatcher) {

    override suspend fun execute(parameters: ModelId) {
        val response = remoteClient.getListTimeline(parameters, count = pageSize * 2)
        val tweets = response.map { it.mapWith(StatusToTweet(parameters).asLambda()) }
        val users = response.map { it.mapWith(StatusToTwitterUser().asLambda()) }
        db.tweetDao().refreshTweetsFromList(parameters, tweets)
        db.twitterUserDao().insert(users)
    }
}
