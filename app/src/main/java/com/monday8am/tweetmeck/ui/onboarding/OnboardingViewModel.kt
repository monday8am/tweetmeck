package com.monday8am.tweetmeck.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.local.PreferenceStorage
import com.monday8am.tweetmeck.util.Event
import kotlinx.coroutines.launch

class OnboardingViewModel(private val preferencesService: PreferenceStorage) : ViewModel() {

    private val _navigateToMainActivity = MutableLiveData<Event<Unit>>()
    val navigateToMainActivity: LiveData<Event<Unit>> = _navigateToMainActivity

    fun getStartedClick() {
        viewModelScope.launch {
            preferencesService.onboardingCompleted = true
            _navigateToMainActivity.value = Event(Unit)
        }
    }
}
