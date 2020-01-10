package com.monday8am.tweetmeck.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.util.Event
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class TimelineViewModel(
    query: TimelineQuery,
    private val authRepository: AuthRepository,
    private val dataRepository: DataRepository
) : ViewModel(),
    TweetItemEventListener {

    private val _navigateToTweetDetails = MutableLiveData<Event<Long>>()
    val navigateToTweetDetails: LiveData<Event<Long>> = _navigateToTweetDetails

    private val _navigateToUserDetails = MutableLiveData<Event<String>>()
    val navigateToUserDetails: LiveData<Event<String>> = _navigateToUserDetails

    private val _navigateToSearch = MutableLiveData<Event<String>>()
    val navigateToSearch: LiveData<Event<String>> = _navigateToSearch

    private val _openUrl = MutableLiveData<Event<String>>()
    val openUrl: LiveData<Event<String>> = _openUrl

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    private val _dataLoading: MutableLiveData<Boolean> = MutableLiveData()
    val dataLoading: LiveData<Boolean> = _dataLoading

    val pagedList: LiveData<PagedList<Tweet>>
    val loadMoreState: LiveData<Result<Unit>>
    var currentSession: Session? = null

    init {
        val timelineContent = dataRepository.getTimeline(query)
        pagedList = timelineContent.pagedList
        loadMoreState = timelineContent.loadMoreState

        viewModelScope.launch {
            authRepository.session.collect { currentSession = it }
        }
    }

    override fun openTweetDetails(tweetId: Long) {
        _navigateToTweetDetails.value = Event(tweetId)
    }

    override fun openUserDetails(screenName: String) {
        _navigateToUserDetails.value = Event(screenName)
    }

    override fun openUrl(url: String) {
        _openUrl.value = Event(url)
    }

    override fun retryLoadMore(listId: Long) {
        Timber.d("retry load more!!")
    }

    override fun searchForTag(tag: String) {
        _navigateToSearch.value = Event(tag)
    }

    override fun searchForSymbol(symbol: String) {
        _navigateToSearch.value = Event(symbol)
    }

    override fun likeTweet(tweet: Tweet) {
        val session = currentSession
        if (session != null) {
            viewModelScope.launch {
                when (val result = dataRepository.likeTweet(tweet, session)) {
                    is Result.Error -> _errorMessage.value =
                        Event(content = result.exception.message ?: "Unknown Error")
                    else -> Timber.d("Tweet updated correctly!")
                }
            }
        } else {
            _errorMessage.value = Event("User must be logged in!")
        }
    }

    override fun retweetTweet(tweet: Tweet) {
        val session = currentSession
        if (session != null) {
            viewModelScope.launch {
                when (val result = dataRepository.retweetTweet(tweet, session)) {
                    is Result.Error -> _errorMessage.value =
                        Event(content = result.exception.message ?: "Unknown Error")
                    else -> Timber.d("Tweet updated correctly!")
                }
            }
        } else {
            _errorMessage.value = Event("User must be logged in!")
        }
    }
}

interface TweetItemEventListener {
    fun openTweetDetails(tweetId: Long)
    fun openUserDetails(screenName: String)
    fun openUrl(url: String)
    fun searchForTag(tag: String)
    fun searchForSymbol(symbol: String)
    fun retryLoadMore(listId: Long)
    fun likeTweet(tweet: Tweet)
    fun retweetTweet(tweet: Tweet)
}
