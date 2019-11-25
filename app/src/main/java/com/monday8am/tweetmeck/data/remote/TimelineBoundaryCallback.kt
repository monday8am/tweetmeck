package com.monday8am.tweetmeck.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.Result.*
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.models.Tweet
import kotlinx.coroutines.*

class TimelineBoundaryCallback(
    private val listId: Long,
    private val remoteSource: TwitterClient,
    private val localSource: TwitterDatabase,
    private val scope: CoroutineScope,
    private val networkPageSize: Int)
    : PagedList.BoundaryCallback<Tweet>() {

    private val tweetDao = localSource.tweetDao()
    private val helper = PagingRequestHelper(Dispatchers.IO.asExecutor())
    val networkState = helper.createStatusLiveData()

    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            scope.launch {
                when (val result = remoteSource.getListTimeline(listId, count = networkPageSize)) {
                    is Success -> {
                        tweetDao.insertTweetsFromList(listId, result.data)
                        it.recordSuccess()
                    }
                    is Error -> { it.recordFailure(result.exception)}
                }
            }
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: Tweet) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            scope.launch {
                when (val result = remoteSource.getListTimeline(listId, itemAtEnd.id, networkPageSize)) {
                    is Success -> {
                        tweetDao.insertTweetsFromList(listId, result.data)
                        it.recordSuccess()
                    }
                    is Error -> { it.recordFailure(result.exception)}
                }
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Tweet) {
        // ignored, since we only ever append to what's in the DB
    }
}

private fun getErrorMessage(report: PagingRequestHelper.StatusReport): String {
    return PagingRequestHelper.RequestType.values().mapNotNull {
        report.getErrorFor(it)?.message
    }.first()
}

fun PagingRequestHelper.createStatusLiveData(): LiveData<Result<Boolean>> {
    val liveData = MutableLiveData<Result<Boolean>>()
    addListener { report ->
        when {
            report.hasRunning() -> liveData.postValue(Loading)
            report.hasError() -> liveData.postValue(Error(Exception(getErrorMessage(report))))
            else -> liveData.postValue(Success(true))
        }
    }
    return liveData
}
