package com.monday8am.tweetmeck.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.util.Event
import kotlinx.coroutines.CoroutineScope
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

interface TimelineViewModelDelegate: TweetItemEventListener {
    val navigateToTweetDetails: LiveData<Event<Long>>
    val navigateToUserDetails: LiveData<Event<String>>
    val navigateToSearch: LiveData<Event<String>>
    val openUrl: LiveData<Event<String>>
    val timelineErrorMessage: LiveData<Event<String>>
    fun likeTweet(tweet: Tweet, session: Session?, scope: CoroutineScope)
    fun retweetTweet(tweet: Tweet, session: Session?, scope: CoroutineScope)
}

class TimelineViewModelDelegateImpl(
    private val dataRepository: DataRepository
) : TimelineViewModelDelegate {

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

    override fun likeTweet(tweet: Tweet) {}

    override fun retweetTweet(tweet: Tweet) {}

    override fun searchForTag(tag: String) {
        _navigateToSearch.value = Event(tag)
    }

    override fun searchForSymbol(symbol: String) {
        _navigateToSearch.value = Event(symbol)
    }

    override fun likeTweet(tweet: Tweet, session: Session?, scope: CoroutineScope) {
        if (session != null) {
            scope.launch {
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

    override fun retweetTweet(tweet: Tweet, session: Session?, scope: CoroutineScope) {
        if (session != null) {
            scope.launch {
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
