package com.monday8am.tweetmeck.ui.home.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.PagedList
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.Result
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.ui.base.TweetListViewModel
import com.monday8am.tweetmeck.ui.delegates.AuthState
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegate
import org.koin.core.context.GlobalContext

class TimelineViewModel(
    listId: Long,
    authRepository: AuthRepository,
    dataRepository: DataRepository
) : TweetListViewModel(authRepository, dataRepository),
    SignInViewModelDelegate by GlobalContext.get().koin.get() {

    override val pagedList: LiveData<PagedList<Tweet>>
    override val loadMoreState: LiveData<Result<Unit>>

    private val _dataLoading: MediatorLiveData<Boolean> = MediatorLiveData()
    override val dataLoading: LiveData<Boolean> = _dataLoading

    init {
        _dataLoading.value = true

        val timelineContent = dataRepository.getTimeline(listId)
        pagedList = timelineContent.pagedList
        loadMoreState = timelineContent.loadMoreState

        _dataLoading.addSource(pagedList) {
            _dataLoading.value = false
        }

        _dataLoading.addSource(authState) { authEvent ->
            when (authEvent.peekContent()) {
                is AuthState.Loading,
                is AuthState.WaitingForUserCredentials -> _dataLoading.value = true
                else -> { }
            }
        }
    }
}
