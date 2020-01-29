package com.monday8am.tweetmeck.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.data.models.TwitterUser
import com.monday8am.tweetmeck.domain.auth.ObserveLoggedSessionUseCase
import com.monday8am.tweetmeck.domain.timeline.GetListTimelineUseCase
import com.monday8am.tweetmeck.domain.timeline.GetSearchTimelineUseCase
import com.monday8am.tweetmeck.domain.tweet.LikeTweetUseCase
import com.monday8am.tweetmeck.domain.tweet.RetweetUseCase
import com.monday8am.tweetmeck.domain.user.GetUserUseCase
import com.monday8am.tweetmeck.ui.timeline.TimelineViewModel
import kotlinx.coroutines.launch

class UserViewModel(
    private val userScreenName: String,
    private val getUserUseCase: GetUserUseCase,
    loggedSessionUseCase: ObserveLoggedSessionUseCase,
    listTimelineUseCase: GetListTimelineUseCase,
    searchTimelineUseCase: GetSearchTimelineUseCase,
    likeTweetUseCase: LikeTweetUseCase,
    retweetUseCase: RetweetUseCase
) : TimelineViewModel(
    loggedSessionUseCase,
    listTimelineUseCase,
    searchTimelineUseCase,
    likeTweetUseCase,
    retweetUseCase) {

    private val _user = MutableLiveData<TwitterUser>()
    val user: LiveData<TwitterUser> = _user

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> = _errorMsg

    init {
        getUserContent()
    }

    private fun getUserContent() {
        viewModelScope.launch {
            _dataLoading.value = true
            when (val result = getUserUseCase(userScreenName)) {
                is Result.Success -> _user.value = result.data
                is Result.Error -> _errorMsg.value = result.exception.toString()
                else -> _dataLoading.value = true
            }

            refreshTimelineContent(TimelineQuery.User(userScreenName))
            _dataLoading.value = false
        }
    }
}
