package com.monday8am.tweetmeck.data.remote

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.Tweet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimelineDbBoundaryCallback(
    private val listId: Long,
    private val firstLoadCallback: suspend (listId: Long) -> Result<Unit>,
    private val loadMoreCallback: suspend (listId: Long, maxTweetId: Long) -> Result<Unit>
) : PagedList.BoundaryCallback<Tweet>(), CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val helper = PagingRequestHelper(Dispatchers.IO.asExecutor())
    val requestState = MutableLiveData<Result<Unit>>()

    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            launch {
                requestState.value = Result.Loading
                val result = withContext(Dispatchers.IO) {
                    firstLoadCallback(listId)
                }
                when (result) {
                    is Result.Success -> it.recordSuccess()
                    is Result.Error -> it.recordFailure(result.exception)
                    else -> it.recordFailure(Exception("Wrong state at TimelineBoundaryCallback!"))
                }
                requestState.value = result
            }
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: Tweet) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            launch {
                requestState.value = Result.Loading
                val result = withContext(Dispatchers.IO) {
                    loadMoreCallback(listId, itemAtEnd.id)
                }
                when (result) {
                    is Result.Success -> it.recordSuccess()
                    is Result.Error -> it.recordFailure(result.exception)
                    else -> it.recordFailure(Exception("Wrong state at TimelineBoundaryCallback!"))
                }
                requestState.value = result
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Tweet) {
        // ignored, since we only ever append to what's in the DB
    }
}
