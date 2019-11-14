package com.monday8am.tweetmeck.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.DataRepository
import kotlinx.coroutines.launch

class TimelineViewModel(private val dataRepository: DataRepository) : ViewModel() {

    fun getList() {
        viewModelScope.launch {
            dataRepository.getLists()
        }
    }
}
