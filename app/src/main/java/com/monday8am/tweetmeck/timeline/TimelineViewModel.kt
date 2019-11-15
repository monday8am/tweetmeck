package com.monday8am.tweetmeck.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.Result.Success
import com.monday8am.tweetmeck.data.Result.Error
import kotlinx.coroutines.launch
import timber.log.Timber

class TimelineViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private val _twitterLists = MutableLiveData<List<TwitterList>>()
    val twitterLists: LiveData<List<TwitterList>> = _twitterLists

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    init {
        loadLists()
    }

    private fun loadLists(forceUpload: Boolean = false) {
        viewModelScope.launch {
            _dataLoading.value = true
            when(val result = dataRepository.getLists(forceUpload)) {
                is Success -> _twitterLists.value = result.data
                is Error -> Timber.d("Error loading lists: ${result.exception.message}")
                else -> Timber.d("Wrong result state!")
            }

            _dataLoading.value = false
        }
    }
}
