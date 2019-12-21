package com.monday8am.tweetmeck.data

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.Result.Success
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.mappers.*
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.data.remote.TimelineBoundaryCallback
import com.monday8am.tweetmeck.data.remote.TwitterClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DataRepository {
    val lists: LiveData<List<TwitterList>>
    suspend fun getTweet(tweetId: Long): Result<Tweet>
    suspend fun getUser(userId: Long): Result<TwitterUser>
    suspend fun refreshListTimeline(listId: Long): Result<Unit>
    suspend fun refreshLists(screenName: String?): Result<Unit>
    suspend fun likeTweet(tweet: Tweet): Result<Unit>
    fun getTimeline(listId: Long, scope: CoroutineScope): TimelineContent
    suspend fun deleteCachedData()
}

// Try order if not update forced: https://medium.com/@appmattus/caching-made-simple-on-android-d6e024e3726b
// 1. Memory
// 2. Local
// 3. Network

class DataRepositoryImpl(
    private val remoteClient: TwitterClient,
    private val db: TwitterDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataRepository {

    private val pageSize = 20
    private val prefetchDistance = 20

    override val lists: LiveData<List<TwitterList>>
        get() = db.twitterListDao().getAll()

    override suspend fun refreshLists(screenName: String?): Result<Unit> {
        return asResult {
            val name = screenName ?: "nytimes"
            val listsFromRemote = remoteClient.getLists(name).map { it.mapTo(ListToTwitterList().toLambda()) }
            db.twitterListDao().updateAll(listsFromRemote)
        }
    }

    override suspend fun refreshListTimeline(listId: Long): Result<Unit> {
        return asResult {
            val response = remoteClient.getListTimeline(listId, count = pageSize * 2)
            val tweets = response.map { it.mapTo(StatusToTweet(listId).toLambda()) }
            val users = response.map { it.mapTo(StatusToTwitterUser().toLambda()) }
            db.tweetDao().insert(tweets)
            db.twitterUserDao().insert(users)
        }
    }

    override suspend fun likeTweet(tweet: Tweet): Result<Unit> {
        return asResult {
            val newTweet = tweet.setFavorite(!tweet.tweetContent.favorited)
            // update cache first to refresh view faster
            if (tweet.isCached) {
                db.tweetDao().insert(newTweet)
            }
            val response = remoteClient.likeTweet(tweet.id, newTweet.tweetContent.favorited)
                                       .mapTo(StatusToTweet(tweet.listId).toLambda())
            // insert tweet with content updated from server
            db.tweetDao().insert(
                newTweet.setRetweetCount(response.tweetContent.retweetCount)
                        .setFavorite(response.tweetContent.favorited)
            )
        }
    }

    private suspend fun loadMoreForTimeline(listId: Long, maxTweetId: Long): Result<Unit> {
        return asResult {
            val result = remoteClient.getListTimeline(listId, maxTweetId = maxTweetId, count = pageSize * 2)
            db.tweetDao().insert(result.map { it.mapTo(StatusToTweet(listId).toLambda()) })
            db.twitterUserDao().insert(result.map { it.mapTo(StatusToTwitterUser().toLambda()) })
        }
    }

    override fun getTimeline(listId: Long, scope: CoroutineScope): TimelineContent {
        val pagedListConfig = PagedList.Config.Builder()
            .setInitialLoadSizeHint(pageSize * 2)
            .setPageSize(pageSize)
            .setPrefetchDistance(prefetchDistance)
            .build()

        val boundaryCallback = TimelineBoundaryCallback(
            listId = listId,
            scope = scope,
            refreshCallback = ::refreshListTimeline,
            loadMoreCallback = ::loadMoreForTimeline
        )

        val livePagedList = db.tweetDao().getTweetsByListId(listId).toLiveData(
                                config = pagedListConfig,
                                boundaryCallback = boundaryCallback)
        return TimelineContent(
            pagedList = livePagedList,
            loadMoreState = boundaryCallback.requestState
        )
    }

    override suspend fun getTweet(tweetId: Long): Result<Tweet> =
        getItemFromDb(tweetId, db.tweetDao()::getItemById
    )

    override suspend fun getUser(userId: Long): Result<TwitterUser> =
        getItemFromDb(userId, db.twitterUserDao()::getItemById
    )

    override suspend fun deleteCachedData() {
        withContext(ioDispatcher) {
            db.twitterListDao().deleteAll()
        }
    }

    private suspend fun <T> getItemFromDb(itemId: Long, dbCall: suspend (Long) -> T?): Result<T> {
        return withContext(ioDispatcher) {
            try {
                val value: T? = dbCall.invoke(itemId)
                if (value != null) {
                    Success(value)
                } else {
                    Error(Exception("Item not found with id: $itemId"))
                }
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    private suspend fun <T> asResult(block: suspend() -> T): Result<T> {
        return withContext(ioDispatcher) {
            try {
                val result = block.invoke()
                Success(result)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }
}

data class TimelineContent(
    val pagedList: LiveData<PagedList<Tweet>>,
    val loadMoreState: LiveData<Result<Unit>>
)
