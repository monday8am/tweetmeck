package com.monday8am.tweetmeck.data.local

import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.Result.Success
import com.monday8am.tweetmeck.data.models.TwitterList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

interface TwitterLocalDataSource {
    suspend fun getTwitterLists(): Result<List<TwitterList>>
    suspend fun insertList(list: TwitterList): Result<Long>
    suspend fun updateAllLists(lists: List<TwitterList>): Result<Unit>
    suspend fun deleteLists()
}

class TwitterDataSourceImpl internal constructor(
    private val listsDao: TwitterListDao,
    private val userDao: TwitterUserDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TwitterLocalDataSource {

    override suspend fun getTwitterLists() = withContext(ioDispatcher) {
        return@withContext try {
            Success(listsDao.getAll())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun insertList(list: TwitterList) = withContext(ioDispatcher) {
        return@withContext try {
            Success(listsDao.insert(list))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun updateAllLists(lists: List<TwitterList>) = withContext(ioDispatcher) {
        return@withContext try {
            Success(listsDao.updateAll(lists))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun deleteLists() = withContext(ioDispatcher) {
        try {
            listsDao.deleteAll()
        } catch (e: Exception) {
           Timber.d("Error deleting lists!")
        }
    }
}
