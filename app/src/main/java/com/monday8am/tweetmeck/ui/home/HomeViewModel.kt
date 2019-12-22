package com.monday8am.tweetmeck.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.TimelineContent
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.succeeded
import com.monday8am.tweetmeck.ui.login.SignInViewModelDelegate
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.map
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import timber.log.Timber

class HomeViewModel(private val dataRepository: DataRepository) :
    ViewModel(), TweetItemEventListener,
        SignInViewModelDelegate by GlobalContext.get().koin.get() {

    val twitterLists: LiveData<List<TwitterList>>
        get() = dataRepository.lists

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    private val _currentUserImageUrl = MutableLiveData<String?>()
    val currentUserImageUrl: LiveData<String?> = _currentUserImageUrl

    private val swipeRefreshResult = MutableLiveData<Result<Unit>>()
    val swipeRefreshing: LiveData<Boolean> = swipeRefreshResult.map {
        false // Whenever refresh finishes, stop the indicator, whatever the result
    }

    private var timelines: MutableMap<Long, TimelineContent> = mutableMapOf()
    private var currentTimelineId: Long = -1

    private val _navigateToTweetDetails = MutableLiveData<Event<Long>>()
    val navigateToTweetDetails: LiveData<Event<Long>> = _navigateToTweetDetails

    private val _navigateToUserDetails = MutableLiveData<Event<Long>>()
    val navigateToUserDetails: LiveData<Event<Long>> = _navigateToUserDetails

    private val _navigateToSignInDialog = MutableLiveData<Event<Boolean>>()
    val navigateToSignInDialog: LiveData<Event<Boolean>> = _navigateToSignInDialog

    init {
        viewModelScope.launch {
            currentSessionFlow.collect { session ->
                refreshUserContent(session)
                refreshLists(session)
            }
        }
    }

    private suspend fun refreshUserContent(session: Session?) {
        if (session != null) {
            when (val result = dataRepository.getUser(session.userId)) {
                is Result.Success -> _currentUserImageUrl.value = result.data.profileImageUrl
                else -> _errorMessage.value = Event("Error loading user profile")
            }
        } else {
            _currentUserImageUrl.value = null
        }
    }

    private suspend fun refreshLists(session: Session?) {
        val result = if (session != null) {
            dataRepository.refreshLoggedUserLists(session)
        } else {
            dataRepository.refreshLists("nytimes")
        }

        if (!result.succeeded) {
            _errorMessage.value = Event("Error loading lists")
        }
    }

    fun getTimelineContent(listId: Long): TimelineContent {
        return timelines.getOrPut(listId, {
            dataRepository.getTimeline(listId, viewModelScope)
        })
    }

    fun onProfileClicked() {
        viewModelScope.launch {
            _navigateToSignInDialog.value = Event(isLogged)
        }
    }

    fun onSwipeRefresh() {
        viewModelScope.launch {
            swipeRefreshResult.value = dataRepository.refreshListTimeline(currentTimelineId)
        }
    }

    fun onChangedDisplayedTimeline(listId: Long) {
        currentTimelineId = listId
    }

    // Tweet actions:

    override fun openTweetDetails(tweetId: Long) {
        _navigateToTweetDetails.value = Event(tweetId)
    }

    override fun openUserDetails(userId: Long) {
        _navigateToUserDetails.value = Event(userId)
    }

    override fun openUserDetails(userIdStr: String) {
        Timber.d("open user details: %s", userIdStr)
    }

    override fun retryLoadMore(listId: Long) {
        Timber.d("retry load more!!")
    }

    override fun likeTweet(tweet: Tweet) {
        if (lastSession != null) {
            viewModelScope.launch {
                when (val result = dataRepository.likeTweet(tweet, lastSession)) {
                    is Error -> _errorMessage.value =
                        Event(content = result.exception.message ?: "Unknown Error")
                    else -> Timber.d("Tweet updated correctly!")
                }
            }
        } else {
            _errorMessage.value = Event("User must be logged in!")
        }
    }

    override fun retweetTweet(tweet: Tweet) {
        if (lastSession != null) {
            viewModelScope.launch {
                when (val result = dataRepository.retweetTweet(tweet, lastSession)) {
                    is Error -> _errorMessage.value =
                        Event(content = result.exception.message ?: "Unknown Error")
                    else -> Timber.d("Tweet updated correctly!")
                }
            }
        } else {
            _errorMessage.value = Event("User must be logged in!")
        }
    }

    override fun openUrl(url: String) {
        Timber.d("open URL: %s", url)
    }

    override fun searchForTag(tag: String) {
        Timber.d("search for TAG: %s", tag)
    }

    override fun searchForSymbol(symbol: String) {
        Timber.d("search for Symbol: %s", symbol)
    }
}

/**
 * Actions that can be performed on tweets.
 */
interface TweetItemEventListener {
    fun openTweetDetails(tweetId: Long)
    fun openUserDetails(userId: Long)
    fun openUserDetails(userIdStr: String)
    fun openUrl(url: String)
    fun searchForTag(tag: String)
    fun searchForSymbol(symbol: String)
    fun retryLoadMore(listId: Long)
    fun likeTweet(tweet: Tweet)
    fun retweetTweet(tweet: Tweet)
}
