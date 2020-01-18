package com.monday8am.tweetmeck.domain.timeline

import androidx.paging.toLiveData
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.TimelineContent
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.mappers.StatusToTweet
import com.monday8am.tweetmeck.data.mappers.StatusToTwitterUser
import com.monday8am.tweetmeck.data.mappers.asLambda
import com.monday8am.tweetmeck.data.mappers.mapWith
import com.monday8am.tweetmeck.data.models.ModelId
import com.monday8am.tweetmeck.data.remote.TimelineDbBoundaryCallback
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.domain.SuspendUseCase
import com.monday8am.tweetmeck.domain.pageSize
import com.monday8am.tweetmeck.domain.pagedListConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class GetListTimelineUseCase constructor(
    private val remoteClient: TwitterClient,
    private val db: TwitterDatabase,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendUseCase<ModelId, TimelineContent>(defaultDispatcher) {

    override suspend fun execute(parameters: ModelId): TimelineContent {
        val boundaryCallback = TimelineDbBoundaryCallback(
            listId = parameters,
            firstLoadCallback = ::firstLoadCallback,
            loadMoreCallback = ::loadMoreForTimeline
        )

        val livePagedList = db.tweetDao().getTweetsByListId(parameters).toLiveData(
            config = pagedListConfig,
            boundaryCallback = boundaryCallback)
        return TimelineContent(
            pagedList = livePagedList,
            loadMoreState = boundaryCallback.requestState
        )
    }

    private suspend fun loadMoreForTimeline(listId: Long, maxTweetId: Long): Result<Unit> {
        return asResult {
            val result = remoteClient.getListTimeline(listId, maxTweetId = maxTweetId, count = pageSize * 2)
            db.tweetDao().insert(result.map { it.mapWith(StatusToTweet(listId).asLambda()) })
            db.twitterUserDao().insert(result.map { it.mapWith(StatusToTwitterUser().asLambda()) })
        }
    }

    private suspend fun firstLoadCallback(listId: Long): Result<Unit> {
        return asResult {
            val response = remoteClient.getListTimeline(listId, count = pageSize * 2)
            val tweets = response.map { it.mapWith(StatusToTweet(listId).asLambda()) }
            val users = response.map { it.mapWith(StatusToTwitterUser().asLambda()) }
            db.tweetDao().refreshTweetsFromList(listId, tweets)
            db.twitterUserDao().insert(users)
        }
    }

    private suspend fun <T> asResult(block: suspend() -> T): Result<T> {
        return withContext(defaultDispatcher) {
            try {
                val result = block.invoke()
                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}
