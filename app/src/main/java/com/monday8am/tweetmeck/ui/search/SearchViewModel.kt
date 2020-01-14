package com.monday8am.tweetmeck.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegate
import com.monday8am.tweetmeck.ui.timeline.TimelineViewModelDelegate
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.map
import com.monday8am.tweetmeck.util.switchMap
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchViewModel(
    private val timelineDelegate: TimelineViewModelDelegate,
    private val dataRepository: DataRepository
    ) : ViewModel(), TimelineViewModelDelegate by timelineDelegate {

    private val _searchQuery = MutableLiveData<Event<TimelineQuery.Hashtag>>()
    val searchQuery: LiveData<Event<TimelineQuery.Hashtag>> = _searchQuery

    val timelineContent: LiveData<PagedList<Tweet>> = _searchQuery.switchMap {
        dataRepository.getTimeline(it.peekContent()).pagedList
    }

    private var lastSession: Session? = null

    init {
        viewModelScope.launch {
            dataRepository.session.collect { session ->
                lastSession = session
            }
        }
    }

    fun searchFor(query: String) {
        // check it first!
        _searchQuery.value = Event(TimelineQuery.Hashtag(query))
    }

    override fun likeTweet(tweet: Tweet) {
        timelineDelegate.likeTweet(tweet, lastSession, viewModelScope)
    }

    override fun retweetTweet(tweet: Tweet) {
        timelineDelegate.retweetTweet(tweet, lastSession, viewModelScope)
    }
}
