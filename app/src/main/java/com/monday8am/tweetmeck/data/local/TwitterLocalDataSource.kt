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
    suspend fun saveList(list: TwitterList)
    suspend fun deleteLists()
}

class TwitterDataSourceImpl internal constructor(
    private val twitterDao: TwitterListDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TwitterLocalDataSource {

    override suspend fun getTwitterLists() = withContext(ioDispatcher) {
        return@withContext try {
            Success(twitterDao.getLists())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun saveList(list: TwitterList) = withContext(ioDispatcher) {
        twitterDao.insertList(list)
    }

    override suspend fun deleteLists() = withContext(ioDispatcher) {
        try {
            twitterDao.deleteLists()
        } catch (e: Exception) {
           Timber.d("Error deleting lists!")
        }
    }

}
