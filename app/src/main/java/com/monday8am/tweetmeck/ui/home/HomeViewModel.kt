package com.monday8am.tweetmeck.ui.home

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.local.PreferenceStorage
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.remote.RequestToken
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

class HomeViewModel @ViewModelInject constructor(
    private val signInDelegate: SignInViewModelDelegate,
    private val observeListUseCase: ObserveListsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val refreshListTimelineUseCase: RefreshListTimelineUseCase,
    private val loadListsFromRemoteUseCase: LoadListsFromRemoteUseCase,
    private val preferences: PreferenceStorage
) : ViewModel(), SignInViewModelDelegate by signInDelegate {

    private val _scrollToTop = MutableLiveData<Int>()
    val scrollToTop: LiveData<Int> = _scrollToTop

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

            _dataLoading.addSource(authState) { authEvent ->
                when (authEvent.peekContent()) {
                    is AuthState.Loading,
                    is AuthState.WaitingForUserCredentials -> _dataLoading.value = true
                    else -> { _dataLoading.value = false }
                }
            }

            observeListUseCase(Unit).collect {
                if (it is Result.Success) {
                    _twitterList.value = it.data
                } else {
                    _errorMessage.value = Event("Error loading list content!")
                }
            }
        }

        viewModelScope.launch {
            observeSession.collect { session ->
                if (session != null) {
                    loadUserContent(session)
                } else {
                    _currentUserImageUrl.value = null
                    loadDefaultLists(session)
                }
            }
        }
    }

    private suspend fun loadUserContent(session: Session) {
        when (val result = getUserUseCase(session.screenName)) {
            is Result.Success -> {
                _currentUserImageUrl.value = result.data.profileImageUrl
                loadListContent(session, result.data.screenName)
            }
            else -> _errorMessage.value = Event("Error loading user profile")
        }
    }

    private suspend fun loadDefaultLists(session: Session?) {
        val result = loadListsFromRemoteUseCase((preferences.initialTopic ?: "ny_times") to session)
        if (!result.succeeded) {
            _errorMessage.value = Event("Error loading lists")
        }
    }

    private suspend fun loadListContent(session: Session, userScreenName: String) {
        val result = loadListsFromRemoteUseCase(userScreenName to session)
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

    fun setScrollToTop(index: Int) {
        _scrollToTop.value = index
    }

    fun triggerLogIn() = viewModelScope.launch { startWebAuth() }

    fun setResult(url: Uri?, token: RequestToken, error: String?) = viewModelScope.launch {
        setWebAuthResult(url, token, error)
    }

    fun triggerLogOut() = viewModelScope.launch { logOut() }
}
