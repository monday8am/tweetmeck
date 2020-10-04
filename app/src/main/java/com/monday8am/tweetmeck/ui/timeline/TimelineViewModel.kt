package com.monday8am.tweetmeck.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.data.data
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.domain.TimelineContent
import com.monday8am.tweetmeck.domain.auth.ObserveLoggedSessionUseCase
import com.monday8am.tweetmeck.domain.timeline.GetListTimelineUseCase
import com.monday8am.tweetmeck.domain.timeline.GetSearchTimelineUseCase
import com.monday8am.tweetmeck.domain.tweet.LikeTweetUseCase
import com.monday8am.tweetmeck.domain.tweet.RetweetUseCase
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.switchMap
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

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

interface TimelineViewModelDelegate : TweetItemEventListener {
    val navigateToTweetDetails: LiveData<Event<Long>>
    val navigateToUserDetails: LiveData<Event<String>>
    val navigateToSearch: LiveData<Event<String>>
    val openUrl: LiveData<Event<String>>
    val timelineErrorMessage: LiveData<Event<String>>
    val pagedList: LiveData<PagedList<Tweet>>
    val loadMoreState: LiveData<Result<Unit>>
}

open class TimelineViewModel(
    private val query: TimelineQuery,
    private val loggedSessionUseCase: ObserveLoggedSessionUseCase,
    private val listTimelineUseCase: GetListTimelineUseCase,
    private val searchTimelineUseCase: GetSearchTimelineUseCase,
    private val likeTweetUseCase: LikeTweetUseCase,
    private val retweetUseCase: RetweetUseCase
) : ViewModel(), TimelineViewModelDelegate {

    private val _navigateToTweetDetails = MutableLiveData<Event<Long>>()
    override val navigateToTweetDetails: LiveData<Event<Long>> = _navigateToTweetDetails

    private val _navigateToUserDetails = MutableLiveData<Event<String>>()
    override val navigateToUserDetails: LiveData<Event<String>> = _navigateToUserDetails

    private val _navigateToSearch = MutableLiveData<Event<String>>()
    override val navigateToSearch: LiveData<Event<String>> = _navigateToSearch

    private val _openUrl = MutableLiveData<Event<String>>()
    override val openUrl: LiveData<Event<String>> = _openUrl

    private val _errorMessage = MutableLiveData<Event<String>>()
    override val timelineErrorMessage: LiveData<Event<String>> = _errorMessage

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _timelineContent = MutableLiveData<TimelineContent>()
    final override val pagedList: LiveData<PagedList<Tweet>>
    final override val loadMoreState: LiveData<Result<Unit>>

    var currentSession: Session? = null

    init {
        viewModelScope.launch {
            loggedSessionUseCase(Unit).collect {
                currentSession = it.data
            }
        }

        viewModelScope.launch {
            val result = when (query) {
                is TimelineQuery.Hashtag -> searchTimelineUseCase(query.hashtag)
                is TimelineQuery.List -> listTimelineUseCase(query.listId)
                else -> Result.Error(Exception("Not implemented query"))
            }

            when (result) {
                is Result.Success -> { _timelineContent.value = result.data }
                is Result.Loading -> _errorMessage.value = Event("Error loading timelime")
                is Result.Error -> _errorMessage.value = Event("Error loading timelime: ${result.exception.message}")
            }
        }

        pagedList = _timelineContent.switchMap { it.pagedList }
        loadMoreState = _timelineContent.switchMap { it.loadMoreState }
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
                when (val result = likeTweetUseCase(tweet to session)) {
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
                when (val result = retweetUseCase(tweet to session)) {
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
