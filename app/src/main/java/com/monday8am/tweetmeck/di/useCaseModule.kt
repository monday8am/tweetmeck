package com.monday8am.tweetmeck.di

import com.monday8am.tweetmeck.domain.auth.GetAuthUrlUseCase
import com.monday8am.tweetmeck.domain.auth.ObserveLoggedSessionUseCase
import com.monday8am.tweetmeck.domain.auth.SignInUseCase
import com.monday8am.tweetmeck.domain.auth.SignOutUseCase
import com.monday8am.tweetmeck.domain.lists.LoadListsFromRemoteUseCase
import com.monday8am.tweetmeck.domain.lists.ObserveListsUseCase
import com.monday8am.tweetmeck.domain.timeline.GetListTimelineUseCase
import com.monday8am.tweetmeck.domain.timeline.GetSearchTimelineUseCase
import com.monday8am.tweetmeck.domain.timeline.RefreshListTimelineUseCase
import com.monday8am.tweetmeck.domain.tweet.GetTweetUseCase
import com.monday8am.tweetmeck.domain.tweet.LikeTweetUseCase
import com.monday8am.tweetmeck.domain.tweet.RetweetUseCase
import com.monday8am.tweetmeck.domain.user.GetUserUseCase
import org.koin.dsl.module

val useCasesModule = module {
    // Auth
    factory { GetAuthUrlUseCase(get()) }
    factory { ObserveLoggedSessionUseCase(get()) }
    factory { SignInUseCase(get(), get()) }
    factory { SignOutUseCase(get()) }
    // lists
    factory { ObserveListsUseCase(get()) }
    factory { LoadListsFromRemoteUseCase(get(), get()) }
    // timeline
    factory { GetListTimelineUseCase(get(), get()) }
    factory { GetSearchTimelineUseCase(get()) }
    factory { RefreshListTimelineUseCase(get(), get()) }
    // tweet
    factory { GetTweetUseCase(get()) }
    factory { LikeTweetUseCase(get(), get()) }
    factory { RetweetUseCase(get(), get()) }
    // user
    factory { GetUserUseCase(get()) }
}
