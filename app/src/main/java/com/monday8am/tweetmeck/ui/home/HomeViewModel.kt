package com.monday8am.tweetmeck.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monday8am.tweetmeck.data.*
import com.monday8am.tweetmeck.data.Result.Error
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.ui.base.TweetListViewModel
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegate
import com.monday8am.tweetmeck.util.Event
import com.monday8am.tweetmeck.util.map
import jp.nephy.penicillin.endpoints.Timeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import timber.log.Timber


class HomeViewModel(
    authRepository: AuthRepository,
    private val dataRepository: DataRepository) :
    TweetListViewModel(authRepository, dataRepository),
        SignInViewModelDelegate by GlobalContext.get().koin.get() {

    val twitterLists: LiveData<List<TwitterList>>
        get() = dataRepository.lists

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _currentUserImageUrl = MutableLiveData<String?>()
    val currentUserImageUrl: LiveData<String?> = _currentUserImageUrl

    private val swipeRefreshResult = MutableLiveData<Result<Unit>>()
    val swipeRefreshing: LiveData<Boolean> = swipeRefreshResult.map {
        false // Whenever refresh finishes, stop the indicator, whatever the result
    }

    private var timelines: MutableMap<Long, TimelineContent> = mutableMapOf()
    private var currentTimelineId: Long = -1

    private val _navigateToSignInDialog = MutableLiveData<Event<Boolean>>()
    val navigateToSignInDialog: LiveData<Event<Boolean>> = _navigateToSignInDialog

    private val _timelineContent = MutableLiveData<TimelineContent>()
    override val timelineContent: LiveData<TimelineContent> = _timelineContent

    init {
        viewModelScope.launch {
            currentSessionFlow.collect { session ->
                refreshUserContent(session)
                refreshLists(session)
            }
        }

        dataRepository.lists.also {

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

    fun onChangedDisplayedTimeline(listId: Long) {
        currentTimelineId = listId
        timelines.getOrPut(listId, {
            dataRepository.getTimeline(listId, viewModelScope)
        })
        _timelineContent.value = timelines[listId]
    }
}
