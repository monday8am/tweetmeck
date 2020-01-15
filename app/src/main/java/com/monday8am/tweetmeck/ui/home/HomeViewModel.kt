package com.monday8am.tweetmeck.ui.home

import androidx.lifecycle.*
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.local.PreferenceStorage
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.succeeded
import com.monday8am.tweetmeck.ui.delegates.AuthState
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegate
import com.monday8am.tweetmeck.ui.timeline.TimelineViewModel
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.map
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(
    private val signInDelegate: SignInViewModelDelegate,
    private val dataRepository: DataRepository,
    private val preferences: PreferenceStorage
) : TimelineViewModel(dataRepository),
    SignInViewModelDelegate by signInDelegate {

    val twitterLists: LiveData<List<TwitterList>>
        get() = Transformations.distinctUntilChanged(dataRepository.lists)

    private val _dataLoading = MediatorLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _currentUserImageUrl = MutableLiveData<String?>()
    val currentUserImageUrl: LiveData<String?> = _currentUserImageUrl

    private val swipeRefreshResult = MutableLiveData<Result<Unit>>()
    val swipeRefreshing: LiveData<Boolean> = swipeRefreshResult.map {
        false // Whenever refresh finishes, stop the indicator, whatever the result
    }

    private val _scrollToTop = MutableLiveData<Event<Unit>>()
    val scrollToTop: LiveData<Event<Unit>> = _scrollToTop

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    private var currentTimelineId: Long = -1

    private val _navigateToSignInDialog = MutableLiveData<Event<Boolean>>()
    val navigateToSignInDialog: LiveData<Event<Boolean>> = _navigateToSignInDialog

    init {
        viewModelScope.launch {
            _dataLoading.value = true
            _dataLoading.addSource(twitterLists) { list ->
                _dataLoading.value = list.isEmpty()
            }
            _dataLoading.addSource(authState) { authEvent ->
                when (authEvent.peekContent()) {
                    is AuthState.Loading,
                    is AuthState.WaitingForUserCredentials -> _dataLoading.value = true
                    else -> { }
                }
            }

            currentSessionFlow.collect { session ->
                refreshUserContent(session)
                refreshLists(session)
            }
        }
    }

    private suspend fun refreshUserContent(session: Session?) {
        if (session != null) {
            when (val result = dataRepository.getUser(session.screenName)) {
                is Result.Success -> _currentUserImageUrl.value = result.data.profileImageUrl
                else -> _errorMessage.value = Event("Error loading user profile")
            }
        } else {
            _currentUserImageUrl.value = null
        }
    }

    private suspend fun refreshLists(session: Session?) {
        timelines.clear()
        val result = if (session != null) {
            dataRepository.refreshLoggedUserLists(session)
        } else {
            dataRepository.refreshLists(preferences.initialTopic ?: "ny_times")
        }

        if (!result.succeeded) {
            _errorMessage.value = Event("Error loading lists")
        }
    }

    fun onProfileClicked() {
        viewModelScope.launch {
            _navigateToSignInDialog.value = Event(isLogged)
        }
    }

    fun onSwipeRefresh() {
        viewModelScope.launch {
            val result = dataRepository.refreshListTimeline(currentTimelineId)
            swipeRefreshResult.value = result
            if (result is Result.Error) {
                _errorMessage.value = Event(result.exception.message ?: "Error refreshing content")
            }
        }
    }

    fun onChangedDisplayedTimeline(listId: Long) {
        currentTimelineId = listId
    }

    fun setScrollToTop() {
        _scrollToTop.value = Event(Unit)
    }
}
