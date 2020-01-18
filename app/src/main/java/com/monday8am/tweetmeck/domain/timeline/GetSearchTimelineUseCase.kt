package com.monday8am.tweetmeck.domain.timeline

import androidx.paging.toLiveData
import com.monday8am.tweetmeck.data.TimelineContent
import com.monday8am.tweetmeck.data.remote.SearchDataSourceFactory
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.domain.SuspendUseCase
import com.monday8am.tweetmeck.domain.pagedListConfig
import com.monday8am.tweetmeck.util.switchMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

open class GetSearchTimelineUseCase(
    private val remoteClient: TwitterClient,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SuspendUseCase<String, TimelineContent>(defaultDispatcher) {

    override suspend fun execute(parameters: String): TimelineContent {
        val source = SearchDataSourceFactory(parameters, remoteClient)
        val requestState = source.sourceLiveData.switchMap { it.requestState }
        return TimelineContent(
            pagedList = source.toLiveData(pagedListConfig),
            loadMoreState = requestState
        )
    }
}
