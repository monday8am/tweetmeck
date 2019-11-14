package com.monday8am.tweetmeck.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.Result.Success
import kotlinx.coroutines.launch

class TimelineViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private val _twitterLists = MutableLiveData<List<TwitterList>>()
    val twitterLists: LiveData<List<TwitterList>> = _twitterLists

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    init {
        loadLists(true)
    }

    private fun loadLists(forceUpload: Boolean = false) {
        viewModelScope.launch {
            _dataLoading.value = true
            val result = dataRepository.getLists(forceUpload)

            if (result is Success) {
                _twitterLists.value = result.data
            } else {
                // Do something!
            }
            _dataLoading.value = false
        }
    }
}
