package com.monday8am.tweetmeck.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.RequestState
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
    val networkState = MutableLiveData<RequestState>() //helper.createStatusLiveData()

    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            networkState.value = Loading
            scope.launch {
                when (val result = remoteSource.getListTimeline(listId, count = networkPageSize * 3)) {
                    is Success -> {
                        tweetDao.insertTweetsFromList(listId, result.data)
                        it.recordSuccess()
                        networkState.value = Success(0)
                    }
                    is Error -> {
                        it.recordFailure(result.exception)
                        networkState.value = Error(result.exception)
                    }
                }
            }
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: Tweet) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            networkState.value = Loading
            scope.launch {
                when (val result = remoteSource.getListTimeline(listId, itemAtEnd.id, networkPageSize)) {
                    is Success -> {
                        // try catch?
                        tweetDao.insertTweetsFromList(listId, result.data)
                        it.recordSuccess()
                        networkState.value = Success(0)
                    }
                    is Error -> {
                        it.recordFailure(result.exception)
                        networkState.value = Error(result.exception)
                    }
                }
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Tweet) {
        // ignored, since we only ever append to what's in the DB
    }
}
