package com.monday8am.tweetmeck.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.models.Tweet
import kotlinx.coroutines.*

class TimelineBoundaryCallback(
    private val listId: Long,
    private val remoteClient: TwitterClient,
    private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val handleResponse: (Long, Result<List<Tweet>>) -> Unit,
    private val networkPageSize: Int)
    : PagedList.BoundaryCallback<Tweet>() {

    private val helper = PagingRequestHelper(ioDispatcher.asExecutor())
    val networkState = helper.createStatusLiveData()

    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            scope.launch {
                val result = remoteClient.getTweetsFromList(listId)
                when (result) {
                    
                }
            }

        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: Tweet) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {

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

fun PagingRequestHelper.createStatusLiveData(): LiveData<RequestState> {
    val liveData = MutableLiveData<RequestState>()
    addListener { report ->
        when {
            report.hasRunning() -> liveData.postValue(RequestState.loaded)
            report.hasError() -> liveData.postValue(
                RequestState.error(getErrorMessage(report)))
            else -> liveData.postValue(RequestState.loaded)
        }
    }
    return liveData
}

