package com.monday8am.tweetmeck.timeline

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.Result.Success
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.util.map
import kotlinx.coroutines.launch
import timber.log.Timber

class TimelineViewModel(private val dataRepository: DataRepository) : ViewModel(), TweetItemEventListener {

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

    init {
        loadLists()
    }

    private fun loadLists(forceUpload: Boolean = false) {
        viewModelScope.launch {
            _dataLoading.value = true
            when (val result = dataRepository.getLists(forceUpload)) {
                is Success -> _twitterLists.value = result.data
                is Error -> Timber.d("Error loading lists: ${result.exception.message}")
                else -> Timber.d("Wrong result state!")
            }

            _dataLoading.value = false
        }
    }

    fun onProfileClicked() {
        Timber.d("OnProfile clicked!")
    }

    fun onSwipeRefresh() {
        Timber.d("OnSwipe refresh!")
    }

    override fun openTweetDetails(tweetId: Long) {

    }

    override fun onUserClicked(tweet: Tweet) {

    }
}

/**
 * Actions that can be performed on tweets.
 */
interface TweetItemEventListener {
    fun openTweetDetails(tweetId: Long)
    fun onUserClicked(tweet: Tweet)
}
