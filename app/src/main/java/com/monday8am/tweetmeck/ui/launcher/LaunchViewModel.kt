package com.monday8am.tweetmeck.ui.launcher

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.monday8am.tweetmeck.data.local.PreferenceStorage
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.map

/**
 * Logic for determining which screen to send users to on app launch.
 */

class LaunchViewModel @ViewModelInject constructor(preferencesStorage: PreferenceStorage) :
    ViewModel() {

    private val onboardingCompletedResult = MutableLiveData<Boolean>()

    val launchDestination: LiveData<Event<LaunchDestination>> = onboardingCompletedResult.map {
        if (it) {
            Event(LaunchDestination.MAIN_ACTIVITY)
        } else {
            Event(LaunchDestination.ONBOARDING)
        }
    }

    init {
        onboardingCompletedResult.value = preferencesStorage.onboardingCompleted
    }
}

enum class LaunchDestination {
    ONBOARDING,
    MAIN_ACTIVITY
}
