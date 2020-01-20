package com.monday8am.tweetmeck.ui.home.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.monday8am.tweetmeck.domain.auth.ObserveLoggedSessionUseCase
import com.monday8am.tweetmeck.domain.timeline.GetListTimelineUseCase
import com.monday8am.tweetmeck.domain.timeline.GetSearchTimelineUseCase
import com.monday8am.tweetmeck.domain.tweet.LikeTweetUseCase
import com.monday8am.tweetmeck.domain.tweet.RetweetUseCase
import com.monday8am.tweetmeck.ui.timeline.TimelineViewModel

class HomePageViewModel(
    loggedSessionUseCase: ObserveLoggedSessionUseCase,
    listTimelineUseCase: GetListTimelineUseCase,
    searchTimelineUseCase: GetSearchTimelineUseCase,
    likeTweetUseCase: LikeTweetUseCase,
    retweetUseCase: RetweetUseCase
) : TimelineViewModel(
    loggedSessionUseCase,
    listTimelineUseCase,
    searchTimelineUseCase,
    likeTweetUseCase,
    retweetUseCase) {

    private val _scrollToTop = MutableLiveData<Int>()
    val scrollToTop: LiveData<Int> = _scrollToTop

    fun setScrollToTop(index: Int) {
        _scrollToTop.value = index
    }
}
