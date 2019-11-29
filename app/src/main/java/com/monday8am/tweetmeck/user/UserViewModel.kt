package com.monday8am.tweetmeck.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.TwitterUser
import kotlinx.coroutines.launch

class UserViewModel(
    private val userId: Long,
    private val dataRepository: DataRepository
) : ViewModel() {

    private val _user = MutableLiveData<Result<TwitterUser>>()
    val user: LiveData<Result<TwitterUser>> = _user

    init {
        getTweetContent()
    }

    private fun getTweetContent() {
        viewModelScope.launch {
            _user.value = Result.Loading
            _user.value = dataRepository.getUser(userId)
        }
    }
}
