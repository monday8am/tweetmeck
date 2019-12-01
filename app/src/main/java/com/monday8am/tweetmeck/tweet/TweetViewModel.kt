package com.monday8am.tweetmeck.tweet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.Tweet
import kotlinx.coroutines.launch

class TweetViewModel(
    private val tweetId: Long,
    private val dataRepository: DataRepository
) : ViewModel() {

    private val _tweet = MutableLiveData<Tweet>()
    val tweet: LiveData<Tweet> = _tweet

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> = _errorMsg

    init {
        getTweetContent()
    }

    private fun getTweetContent() {
        viewModelScope.launch {
            _dataLoading.value = true
            when (val result = dataRepository.getTweet(tweetId)) {
                is Result.Success -> _tweet.value = result.data
                is Result.Error -> _errorMsg.value = result.exception.toString()
                else -> _dataLoading.value = true
            }
            _dataLoading.value = false
        }
    }
}
