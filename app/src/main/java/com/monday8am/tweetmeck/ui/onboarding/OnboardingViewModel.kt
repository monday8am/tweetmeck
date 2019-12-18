package com.monday8am.tweetmeck.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.local.SharedPreferencesService
import com.monday8am.tweetmeck.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardingViewModel(private val preferencesService: SharedPreferencesService) : ViewModel() {

    private val _onboardingPresented = MutableLiveData<Event<Boolean>>()
    val onboardingPresented: LiveData<Event<Boolean>> = _onboardingPresented

    init {
        viewModelScope.launch {
            val event = withContext(Dispatchers.IO) {
                 Event(preferencesService.isOnboardingPresented())
            }
            _onboardingPresented.value = event
        }
    }

    fun saveOnboardingPresented() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                preferencesService.saveOnboardingState(true)
            }
            _onboardingPresented.value = Event(true)
        }
    }
}
