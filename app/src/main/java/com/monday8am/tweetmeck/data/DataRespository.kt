package com.monday8am.tweetmeck.data

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.Result.Success
import com.monday8am.tweetmeck.data.local.TwitterLocalDataSource
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.data.remote.RequestState
import com.monday8am.tweetmeck.data.remote.TwitterClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

interface DataRepository {
    suspend fun getUser(forceUpdate: Boolean = false): Result<TwitterUser>
    suspend fun getLists(forceUpdate: Boolean = false): Result<List<TwitterList>>
    suspend fun getListTimeline(listId: Long, forceUpdate: Boolean): TimelineContent
    suspend fun deleteCachedData()
}

// Try order if not update forced: https://medium.com/@appmattus/caching-made-simple-on-android-d6e024e3726b
// 1. Memory
// 2. Local
// 3. Network

class DataRepositoryImpl(
    private val twitterClient: TwitterClient,
    private val localDataSource: TwitterLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataRepository {

    private val pageSize = 30

    private var cachedLists: List<TwitterList>? = null

    private var timelines: Map<Long, TimelineContent> = emptyMap()

    private val pagedListConfig = PagedList.Config.Builder()
                                                    .setInitialLoadSizeHint(pageSize * 2)
                                                    .setPageSize(pageSize)
                                                    .setPrefetchDistance(10)
                                                    .build()

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

    override suspend fun getListTimeline(listId: Long, forceUpdate: Boolean): TimelineContent {
        val boundaryCallback = TimelineBoundaryCallback(
            webservice = redditApi,
            subredditName = subReddit,
            handleResponse = this::insertResultIntoDb,
            ioExecutor = ioExecutor,
            networkPageSize = networkPageSize)
        return Result.Loading
    }

    override suspend fun deleteCachedData() {
        return withContext(ioDispatcher) {
            localDataSource.deleteLists()
        }
    }

    private suspend fun fetchListsFromRemoteOrLocal(forceUpdate: Boolean): Result<List<TwitterList>> {
        // Don't read from local if it's forced
        if (forceUpdate) {
            try {
                val listsFromRemote = twitterClient.getLists()
                localDataSource.updateAllLists(listsFromRemote)
                return Success(listsFromRemote)
            } catch (error: Throwable) {
                Timber.d("Remote data source fetch failed : ${error.message}")
            }
            return Error(Exception("Can't force refresh: remote data source is unavailable"))
        }

        // Local if remote fails
        val localLists = localDataSource.getTwitterLists()
        if (localLists is Success) return localLists
        return Error(Exception("Error fetching from remote and local"))
    }

    private fun insertResultIntoDb(listId: Long, content: List<Tweet>) {
        body!!.data.children.let { posts ->
            db.runInTransaction {
                val start = db.posts().getNextIndexInSubreddit(subredditName)
                val items = posts.mapIndexed { index, child ->
                    child.data.indexInResponse = start + index
                    child.data
                }
                db.posts().insert(items)
            }
        }
    }
}

data class TimelineContent(
    val pagedList: LiveData<PagedList<Tweet>>,
    val loadMoreState: LiveData<RequestState>,
    val refreshState: LiveData<RequestState>)
