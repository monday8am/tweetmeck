package com.monday8am.tweetmeck.data.remote

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.mappers.StatusToTweet
import com.monday8am.tweetmeck.data.mappers.asLambda
import com.monday8am.tweetmeck.data.mappers.mapWith
import com.monday8am.tweetmeck.data.models.Tweet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchDataSourceFactory(
    private val query: String,
    private val client: TwitterClient
) : DataSource.Factory<Long, Tweet>() {
    val sourceLiveData = MutableLiveData<SearchDataSource>()
    override fun create(): DataSource<Long, Tweet> {
        val source = SearchDataSource(query, client)
        sourceLiveData.postValue(source)
        return source
    }
}

class SearchDataSource(
    private val query: String,
    private val client: TwitterClient
) : ItemKeyedDataSource<Long, Tweet>(), CoroutineScope by CoroutineScope(Dispatchers.Main) {

    val requestState = MutableLiveData<Result<Unit>>()

    override fun getKey(item: Tweet): Long = item.id

    override fun loadAfter(
        params: LoadParams<Long>,
        callback: LoadCallback<Tweet>
    ) = searchQuery(query, params.key, callback)

    override fun loadInitial(
        params: LoadInitialParams<Long>,
        callback: LoadInitialCallback<Tweet>
    ) = searchQuery(query, null, callback)

    private fun searchQuery(query: String, key: Long?, callback: LoadCallback<Tweet>) {
        launch {
            requestState.value = Result.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    client.search(query, key)
                          .map { it.mapWith(StatusToTweet(-1).asLambda()) }
                }
                requestState.value = Result.Success(
                    callback.onResult(result)
                )
            } catch (e: Exception) {
                requestState.value = Result.Error(e)
            }
        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Tweet>) {
        // ignored, since we only ever append to our initial load
    }
}
