package com.monday8am.tweetmeck.data

import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.Result.Success
import com.monday8am.tweetmeck.data.local.TwitterLocalDataSource
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.data.remote.TwitterClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

interface DataRepository {
    suspend fun getUser(forceUpdate: Boolean = false): Result<TwitterUser>
    suspend fun getLists(forceUpdate: Boolean = false): Result<List<TwitterList>>
    suspend fun getListTimeline(forceUpdate: Boolean = false): Result<List<Tweet>>
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

    override suspend fun getListTimeline(forceUpdate: Boolean): Result<List<Tweet>> {
        /*
        withContext(ioDispatcher) {

        }
        */
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

    /*
    private suspend fun fetchTweetsFromRemoteOrLocal(listId: Long,
                                                     forceUpdate: Boolean): Result<List<Tweet>> {
        // Don't read from local if it's forced
        if (forceUpdate) {
            try {
                val tweetsFromRemote = twitterClient.getTweetsFromList(listId)
                localDataSource.insertTweets(tweetsFromRemote)
                return Success(tweetsFromRemote)
            } catch (error: Throwable) {
                Timber.d("Remote data source fetch failed : ${error.message}")
            }
            return Error(Exception("Can't force refresh: remote data source is unavailable"))
        }

        // Local if remote fails
        val localTweets = localDataSource.getTweetsFromList(listId)
        if (localTweets is Success) return localTweets
        return Error(Exception("Error fetching from remote and local"))
    }
     */

}
