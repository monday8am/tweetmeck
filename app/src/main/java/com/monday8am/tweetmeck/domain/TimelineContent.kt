package com.monday8am.tweetmeck.domain

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.Tweet

data class TimelineContent(
    val pagedList: LiveData<PagedList<Tweet>>,
    val loadMoreState: LiveData<Result<Unit>>
)
