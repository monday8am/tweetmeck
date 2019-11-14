package com.monday8am.tweetmeck.data

import com.monday8am.tweetmeck.data.Result.Success
import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.local.TwitterLocalDataSource
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.remote.TwitterClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

interface DataRepository {
    suspend fun getLists(forceUpdate: Boolean = false): Result<List<TwitterList>>
    suspend fun deleteCachedData()
}

class DefaultDataRepository(
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
                if (it.data.isEmpty()) {
                    return@withContext Success(it.data)
                }
            }

            return@withContext Error(Exception("Illegal state"))
        }
    }

    override suspend fun deleteCachedData() {
        return withContext(ioDispatcher) {
            localDataSource.deleteLists()
        }
    }

    private suspend fun fetchListsFromRemoteOrLocal(forceUpdate: Boolean): Result<List<TwitterList>> {
        try {
            val listsFromRemote = twitterClient.getUserLists()
            localDataSource.deleteLists()
            for (list in listsFromRemote) {
                localDataSource.saveList(list)
            }

        } catch (error: Throwable) {
            Timber.w("Remote data source fetch failed : ${error.message}")
        }

        // Don't read from local if it's forced
        if (forceUpdate) {
            return Error(Exception("Can't force refresh: remote data source is unavailable"))
        }

        // Local if remote fails
        val localTasks = localDataSource.getTwitterLists()
        if (localTasks is Success) return localTasks
        return Error(Exception("Error fetching from remote and local"))
    }

}
