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
import timber.log.Timber

class SearchViewModel(
    query: TimelineQuery,
    loggedSessionUseCase: ObserveLoggedSessionUseCase,
    listTimelineUseCase: GetListTimelineUseCase,
    searchTimelineUseCase: GetSearchTimelineUseCase,
    likeTweetUseCase: LikeTweetUseCase,
    retweetUseCase: RetweetUseCase
) : TimelineViewModel(
    query,
    loggedSessionUseCase,
    listTimelineUseCase,
    searchTimelineUseCase,
    likeTweetUseCase,
    retweetUseCase
) {
    private val _searchQuery = MutableLiveData<Event<TimelineQuery.Hashtag>>()
    val searchQuery: LiveData<Event<TimelineQuery.Hashtag>> = _searchQuery

    fun searchFor(query: String) {
        // TODO: check it first!
        val timelineQuery = TimelineQuery.Hashtag(query)
        _searchQuery.value = Event(timelineQuery)
        // refreshTimelineContent(timelineQuery)
    }

    override fun searchForTag(tag: String) {
        // _navigateToSearch.value = Event(tag)
        Timber.d("Tag: $tag!")
    }
}
