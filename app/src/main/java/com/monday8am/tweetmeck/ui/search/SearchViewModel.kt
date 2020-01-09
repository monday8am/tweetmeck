package com.monday8am.tweetmeck.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.util.Event

class SearchViewModel : ViewModel() {

    private val _searchQuery = MutableLiveData<Event<TimelineQuery.Hashtag>>()
    val searchQuery: LiveData<Event<TimelineQuery.Hashtag>> = _searchQuery

    fun searchFor(query: String) {
        // check it first!
        _searchQuery.value = Event(TimelineQuery.Hashtag(query))
    }
}
