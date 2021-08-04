package com.monday8am.tweetmeck.domain.timeline

import androidx.paging.toLiveData
import com.monday8am.tweetmeck.data.remote.SearchDataSourceFactory
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.di.IoDispatcher
import com.monday8am.tweetmeck.domain.TimelineContent
import com.monday8am.tweetmeck.domain.UseCase
import com.monday8am.tweetmeck.domain.pagedListConfig
import com.monday8am.tweetmeck.util.switchMap
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class GetSearchTimelineUseCase @Inject constructor(
    private val remoteClient: TwitterClient,
    @IoDispatcher defaultDispatcher: CoroutineDispatcher
) : UseCase<String, TimelineContent>(defaultDispatcher) {

    override fun execute(parameters: String): TimelineContent {
        val source = SearchDataSourceFactory(parameters, remoteClient)
        val requestState = source.sourceLiveData.switchMap { it.requestState }
        return TimelineContent(
            pagedList = source.toLiveData(pagedListConfig),
            loadMoreState = requestState
        )
    }
}
