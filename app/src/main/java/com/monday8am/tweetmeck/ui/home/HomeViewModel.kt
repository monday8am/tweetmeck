package com.monday8am.tweetmeck.ui.home

import androidx.lifecycle.*
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.succeeded
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegate
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.map
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class HomeViewModel(
    private val dataRepository: DataRepository
) : ViewModel(),
        SignInViewModelDelegate by GlobalContext.get().koin.get() {

    val twitterLists: LiveData<List<TwitterList>>
        get() = Transformations.distinctUntilChanged(dataRepository.lists)

    private val _dataLoading = MutableLiveData<Boolean>()
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
            currentSessionFlow.collect { session ->
                refreshUserContent(session)
                refreshLists(session)
                _dataLoading.value = false
            }
        }
    }

    private suspend fun refreshUserContent(session: Session?) {
        if (session != null) {
            when (val result = dataRepository.getUser(session.userId)) {
                is Result.Success -> _currentUserImageUrl.value = result.data.profileImageUrl
                else -> _errorMessage.value = Event("Error loading user profile")
            }
        } else {
            _currentUserImageUrl.value = null
        }
    }

    private suspend fun refreshLists(session: Session?) {
        val result = if (session != null) {
            dataRepository.refreshLoggedUserLists(session)
        } else {
            dataRepository.refreshLists("nytimes")
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
            swipeRefreshResult.value = dataRepository.refreshListTimeline(currentTimelineId)
        }
    }

    fun onChangedDisplayedTimeline(position: Int) {
        twitterLists.value?.let {
            if (it.isNotEmpty()) {
                currentTimelineId = it[position].id
            }
        }
    }
}
