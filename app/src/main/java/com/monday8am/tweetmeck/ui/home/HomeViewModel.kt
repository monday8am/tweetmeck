package com.monday8am.tweetmeck.ui.home

import androidx.lifecycle.*
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.local.PreferenceStorage
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.succeeded
import com.monday8am.tweetmeck.domain.lists.LoadListsFromRemoteUseCase
import com.monday8am.tweetmeck.domain.lists.ObserveListsUseCase
import com.monday8am.tweetmeck.domain.timeline.RefreshListTimelineUseCase
import com.monday8am.tweetmeck.domain.user.GetUserUseCase
import com.monday8am.tweetmeck.ui.delegates.AuthState
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegate
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.map
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val signInDelegate: SignInViewModelDelegate,
    private val observeListUseCase: ObserveListsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val refreshListTimelineUseCase: RefreshListTimelineUseCase,
    private val loadListsFromRemoteUseCase: LoadListsFromRemoteUseCase,
    private val preferences: PreferenceStorage
) : ViewModel(), SignInViewModelDelegate by signInDelegate {

    private val _twitterList = MutableLiveData<List<TwitterList>>()
    val twitterLists: LiveData<List<TwitterList>> = _twitterList

    private val _dataLoading = MediatorLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _currentUserImageUrl = MutableLiveData<String?>()
    val currentUserImageUrl: LiveData<String?> = _currentUserImageUrl

    private val swipeRefreshResult = MutableLiveData<Result<Unit>>()
    val swipeRefreshing: LiveData<Boolean> = swipeRefreshResult.map {
        false // Whenever refresh finishes, stop the indicator, whatever the result
    }

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

            observeListUseCase(Unit).collect {
                if (it is Result.Success) {
                    _twitterList.value = it.data
                } else {
                    _errorMessage.value = Event("Error loading list content!")
                }
            }

            observeSession.collect { session ->
                loadUserContent(session)
                loadListContent(session)
            }
        }
    }

    private suspend fun loadUserContent(session: Session?) {
        if (session != null) {
            when (val result = getUserUseCase(session.screenName)) {
                is Result.Success -> _currentUserImageUrl.value = result.data.profileImageUrl
                else -> _errorMessage.value = Event("Error loading user profile")
            }
        } else {
            _currentUserImageUrl.value = null
        }
    }

    private suspend fun loadListContent(session: Session?) {
        val result = loadListsFromRemoteUseCase((preferences.initialTopic ?: "ny_times") to session)
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
            val result = refreshListTimelineUseCase(currentTimelineId)
            swipeRefreshResult.value = result
            if (result is Result.Error) {
                _errorMessage.value = Event(result.exception.message ?: "Error refreshing content")
            }
        }
    }

    fun onChangedDisplayedTimeline(listId: Long) {
        currentTimelineId = listId
    }
}
