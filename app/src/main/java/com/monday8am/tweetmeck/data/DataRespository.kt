package com.monday8am.tweetmeck.data

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.Result.Success
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.data.models.isCached
import com.monday8am.tweetmeck.data.remote.TimelineBoundaryCallback
import com.monday8am.tweetmeck.data.remote.TwitterClient
import kotlinx.coroutines.*
import timber.log.Timber

interface DataRepository {
    suspend fun getTweet(tweetId: Long): Result<Tweet>
    suspend fun getUser(userId: Long): Result<TwitterUser>
    suspend fun getLists(forceUpdate: Boolean = false): Result<List<TwitterList>>
    suspend fun refreshTimeline(listId: Long): Result<Boolean>
    suspend fun likeTweet(tweet: Tweet): Result<Boolean>
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
    private var cachedLists: List<TwitterList>? = null

    override suspend fun getLists(forceUpdate: Boolean): Result<List<TwitterList>> {
        return withContext(ioDispatcher) {
            if (!forceUpdate) {
                cachedLists?.let { cached ->
                    return@withContext Success(cached.sortedBy { it.createdAt })
                }
            }
            val newTasks = fetchListsFromRemoteOrLocal(forceUpdate)
            (newTasks as? Success)?.let {
                cachedLists = it.data
            }
            return@withContext newTasks
        }
    }

    override suspend fun refreshTimeline(listId: Long): Result<Boolean> {
        return withContext(ioDispatcher) {
            when (val result = remoteClient.getListTimeline(listId, count = pageSize * 2)) {
                is Success -> {
                    db.twitterUserDao().insert(result.data.map { it.user })
                    db.tweetDao().refreshTweetsFromList(listId, result.data.map { it.tweet })
                    Success(true)
                }
                is Error -> Error(result.exception)
                else -> Error(Exception("Wrong state at refresh operation"))
            }
        }
    }

    override suspend fun likeTweet(tweet: Tweet): Result<Boolean> {
        return withContext(ioDispatcher) {
            var newTweet = tweet.copy(
                favorited = !tweet.favorited,
                favoriteCount = if(!tweet.favorited) tweet.favoriteCount + 1 else tweet.favoriteCount - 1)

            // update cache first to refresh view faster
            if (tweet.isCached()) {
                db.tweetDao().insert(newTweet)
            }

            when (val result = remoteClient.likeTweet(tweet.id, newTweet.favorited)) {
                is Success -> {
                    if (tweet.isCached()) {
                        newTweet = newTweet.copy(retweetCount = result.data.retweetCount)
                        db.tweetDao().insert(newTweet)
                    }
                    Success(true)
                }
                is Error -> Error(result.exception)
                else -> Error(Exception("Wrong state at refresh operation"))
            }
        }
    }

    private suspend fun loadMoreForTimeline(listId: Long, maxTweetId: Long): Result<Boolean> {
        return withContext(ioDispatcher) {
            when (val result = remoteClient.getListTimeline(listId, maxTweetId = maxTweetId, count = pageSize * 2)) {
                is Success -> {
                        db.twitterUserDao().insert(result.data.map { it.user })
                        db.tweetDao().insert(result.data.map { it.tweet })
                    Success(true)
                }
                is Error -> Error(result.exception)
                else -> Error(Exception("Wrong state at refresh operation"))
            }
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
            refreshCallback = ::refreshTimeline,
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

    private suspend fun fetchListsFromRemoteOrLocal(forceUpdate: Boolean): Result<List<TwitterList>> {
        // Don't read from local if it's forced
        if (forceUpdate) {
            try {
                val listsFromRemote = remoteClient.getLists()
                db.twitterListDao().updateAll(listsFromRemote)
                return Success(listsFromRemote)
            } catch (error: Throwable) {
                Timber.d("Remote data source fetch failed : ${error.message}")
            }
            return Error(Exception("Can't force refresh: remote data source is unavailable"))
        }

        // Local if remote fails
        return try {
            Success(db.twitterListDao().getAll())
        } catch (e: Throwable) {
            Error(Exception("Error fetching from remote and local: ${e.message}"))
        }
    }
}

data class TimelineContent(
    val pagedList: LiveData<PagedList<Tweet>>,
    val loadMoreState: LiveData<RequestState>
)
