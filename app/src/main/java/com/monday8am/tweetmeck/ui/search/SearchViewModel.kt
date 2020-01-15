package com.monday8am.tweetmeck.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.ui.timeline.TimelineViewModel
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.switchMap

class SearchViewModel(
    private val dataRepository: DataRepository
) : TimelineViewModel(dataRepository) {

    private val _searchQuery = MutableLiveData<Event<TimelineQuery.Hashtag>>()
    val searchQuery: LiveData<Event<TimelineQuery.Hashtag>> = _searchQuery

    val timelineContent: LiveData<PagedList<Tweet>> = _searchQuery.switchMap {
        dataRepository.getTimeline(it.peekContent()).pagedList
    }

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    fun searchFor(query: String) {
        // check it first!
        _searchQuery.value = Event(TimelineQuery.Hashtag(query))
    }
}
