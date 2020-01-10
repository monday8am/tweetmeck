package com.monday8am.tweetmeck.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.TwitterUser
import kotlinx.coroutines.launch

class UserViewModel(
    private val userScreenName: String,
    private val dataRepository: DataRepository
) : ViewModel() {

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
            when (val result = dataRepository.getUser(userScreenName)) {
                is Result.Success -> _user.value = result.data
                is Result.Error -> _errorMsg.value = result.exception.toString()
                else -> _dataLoading.value = true
            }
            _dataLoading.value = false
        }
    }
}
