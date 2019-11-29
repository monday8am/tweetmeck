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

    private val _tweet = MutableLiveData<Result<Tweet>>()
    val tweet: LiveData<Result<Tweet>> = _tweet

    init {
        getTweetContent()
    }

    private fun getTweetContent() {
        viewModelScope.launch {
            _tweet.value = Result.Loading
            _tweet.value = dataRepository.getTweet(tweetId)
        }
    }
}
