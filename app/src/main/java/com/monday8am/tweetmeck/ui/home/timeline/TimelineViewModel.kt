package com.monday8am.tweetmeck.ui.home.timeline

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.ui.base.TweetListViewModel

class TimelineViewModel(
    listId: Long,
    authRepository: AuthRepository,
    dataRepository: DataRepository
) : TweetListViewModel(authRepository, dataRepository) {

    override val pagedList: LiveData<PagedList<Tweet>>
    override val loadMoreState: LiveData<Result<Unit>>

    init {
        val timelineContent = dataRepository.getTimeline(listId)
        pagedList = timelineContent.pagedList
        loadMoreState = timelineContent.loadMoreState
    }
}
