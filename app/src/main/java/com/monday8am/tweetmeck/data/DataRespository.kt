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
import com.monday8am.tweetmeck.data.remote.TimelineBoundaryCallback
import com.monday8am.tweetmeck.data.remote.TwitterClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

interface DataRepository {
    suspend fun getUser(forceUpdate: Boolean = false): Result<TwitterUser>
    suspend fun getLists(forceUpdate: Boolean = false): Result<List<TwitterList>>
    fun getListTimeline(listId: Long, scope: CoroutineScope): TimelineContent
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

    override suspend fun getUser(forceUpdate: Boolean): Result<TwitterUser> {
        return Error(Exception("Not implemented!"))
    }

    override fun getListTimeline(listId: Long, scope: CoroutineScope): TimelineContent {
        val networkPageSize = 20
        val pageSize = 20

        val pagedListConfig = PagedList.Config.Builder()
            .setInitialLoadSizeHint(pageSize * 2)
            .setPageSize(pageSize)
            .setPrefetchDistance(10)
            .build()

        val boundaryCallback = TimelineBoundaryCallback(
            listId = listId,
            remoteSource = remoteClient,
            localSource = db,
            scope = scope,
            networkPageSize = networkPageSize
        )
        val livePagedList = db.tweetDao().getTweetsByListId(listId).toLiveData(
                                config = pagedListConfig,
                                boundaryCallback = boundaryCallback)
        return TimelineContent(
            pagedList = livePagedList,
            loadMoreState = boundaryCallback.networkState,
            refreshState = boundaryCallback.networkState
        )
    }

    override suspend fun deleteCachedData() {
        withContext(ioDispatcher) {
            db.twitterListDao().deleteAll()
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

    private suspend fun insertResultIntoDb(listId: Long, content: List<Tweet>) {
        withContext(ioDispatcher) {
            db.tweetDao().insertTweetsFromList(listId, content)
        }
    }
}

data class TimelineContent(
    val pagedList: LiveData<PagedList<Tweet>>,
    val loadMoreState: LiveData<RequestState>,
    val refreshState: LiveData<RequestState>)
