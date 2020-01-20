package com.monday8am.tweetmeck.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.domain.auth.ObserveLoggedSessionUseCase
import com.monday8am.tweetmeck.domain.timeline.GetListTimelineUseCase
import com.monday8am.tweetmeck.domain.timeline.GetSearchTimelineUseCase
import com.monday8am.tweetmeck.domain.tweet.LikeTweetUseCase
import com.monday8am.tweetmeck.domain.tweet.RetweetUseCase
import com.monday8am.tweetmeck.ui.timeline.TimelineViewModel
import com.monday8am.tweetmeck.util.Event

class SearchViewModel(
    loggedSessionUseCase: ObserveLoggedSessionUseCase,
    listTimelineUseCase: GetListTimelineUseCase,
    searchTimelineUseCase: GetSearchTimelineUseCase,
    likeTweetUseCase: LikeTweetUseCase,
    retweetUseCase: RetweetUseCase
) : TimelineViewModel(
    loggedSessionUseCase,
    listTimelineUseCase,
    searchTimelineUseCase,
    likeTweetUseCase,
    retweetUseCase
) {
    private val _searchQuery = MutableLiveData<Event<TimelineQuery.Hashtag>>()
    val searchQuery: LiveData<Event<TimelineQuery.Hashtag>> = _searchQuery

    fun searchFor(query: String) {
        // check it first!
        val timelineQuery = TimelineQuery.Hashtag(query)
        _searchQuery.value = Event(timelineQuery)
        refreshTimelineContent(timelineQuery)
    }
}
