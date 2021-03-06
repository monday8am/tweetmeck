package com.monday8am.tweetmeck.di

import com.monday8am.tweetmeck.BuildConfig
import com.monday8am.tweetmeck.MainActivity
import com.monday8am.tweetmeck.data.local.PreferenceStorage
import com.monday8am.tweetmeck.data.local.SharedPreferencesServiceImpl
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.data.remote.TwitterClientImpl
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegate
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegateImpl
import com.monday8am.tweetmeck.ui.home.HomeViewModel
import com.monday8am.tweetmeck.ui.home.page.HomePageViewModel
import com.monday8am.tweetmeck.ui.launcher.LaunchViewModel
import com.monday8am.tweetmeck.ui.login.AuthViewModel
import com.monday8am.tweetmeck.ui.onboarding.OnboardingViewModel
import com.monday8am.tweetmeck.ui.search.SearchViewModel
import com.monday8am.tweetmeck.ui.tweet.TweetViewModel
import com.monday8am.tweetmeck.ui.user.UserViewModel
import com.monday8am.tweetmeck.util.TimelinePoolProvider
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single { TwitterDatabase.create(androidContext()) }
    single<PreferenceStorage> { SharedPreferencesServiceImpl(androidContext()) }
    single<TwitterClient> { TwitterClientImpl(
        BuildConfig.apiKey,
        BuildConfig.apiSecret,
        BuildConfig.accessToken,
        BuildConfig.accessTokenSecret,
        BuildConfig.callbackUrl) }

    factory { get<TwitterDatabase>().tweetDao() }
    factory { get<TwitterDatabase>().twitterListDao() }
    factory { get<TwitterDatabase>().twitterUserDao() }

    single<SignInViewModelDelegate> {
        SignInViewModelDelegateImpl(
            get(), get(), get(), get()
        )
    }

    viewModel { LaunchViewModel(get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { HomePageViewModel(get(), get(), get(), get(), get()) }
    viewModel { (screenName: String) -> UserViewModel(screenName, get()) }
    viewModel { (tweetId: Long) -> TweetViewModel(tweetId, get()) }
    viewModel { SearchViewModel(get(), get(), get(), get(), get()) }

    scope(named<MainActivity>()) {
        scoped { TimelinePoolProvider() }
    }
}
