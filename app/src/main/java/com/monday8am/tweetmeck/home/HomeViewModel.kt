package com.monday8am.tweetmeck.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.Result.Success
import com.monday8am.tweetmeck.data.TimelineContent
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.map
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(private val dataRepository: DataRepository) : ViewModel(), TweetItemEventListener {

    private val _twitterLists = MutableLiveData<List<TwitterList>>()
    val twitterLists: LiveData<List<TwitterList>> = _twitterLists

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _currentUserImageUri = MutableLiveData<Uri>()
    val currentUserImageUri: LiveData<Uri> = _currentUserImageUri

    private val swipeRefreshResult = MutableLiveData<Result<Boolean>>()
    val swipeRefreshing: LiveData<Boolean> = swipeRefreshResult.map {
        false // Whenever refresh finishes, stop the indicator, whatever the result
    }

    private var timelines: MutableMap<Long, TimelineContent> = mutableMapOf()
    private var currentTimelineId: Long = -1

    private val _navigateToTweetDetails = MutableLiveData<Event<Long>>()
    val navigateToTweetDetails: LiveData<Event<Long>> = _navigateToTweetDetails

    private val _navigateToUserDetails = MutableLiveData<Event<Long>>()
    val navigateToUserDetails: LiveData<Event<Long>> = _navigateToUserDetails

    init {
        loadLists(true)
    }

    private fun loadLists(forceUpload: Boolean = false) {
        viewModelScope.launch {
            _dataLoading.value = true
            when (val result = dataRepository.getLists(forceUpload)) {
                is Success -> _twitterLists.value = result.data.take(1)
                is Error -> Timber.d("Error loading lists: ${result.exception.message}")
                else -> Timber.d("Wrong result state!")
            }
            _dataLoading.value = false
        }
    }

    fun getTimelineContent(listId: Long): TimelineContent {
        return timelines.getOrPut(listId, {
            dataRepository.getTimeline(listId, viewModelScope)
        })
    }

    fun onProfileClicked() {
        Timber.d("OnProfile clicked!")
    }

    fun onSwipeRefresh() {
        viewModelScope.launch {
            swipeRefreshResult.value = dataRepository.refreshTimeline(currentTimelineId)
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
}
