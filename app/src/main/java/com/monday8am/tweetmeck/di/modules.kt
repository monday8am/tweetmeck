package com.monday8am.tweetmeck.di

import com.monday8am.tweetmeck.BuildConfig
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.AuthRepositoryImpl
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.DataRepositoryImpl
import com.monday8am.tweetmeck.data.local.SharedPreferencesService
import com.monday8am.tweetmeck.data.local.SharedPreferencesServiceImpl
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.data.remote.TwitterClientImpl
import com.monday8am.tweetmeck.ui.home.HomeFragment
import com.monday8am.tweetmeck.ui.home.HomeViewModel
import com.monday8am.tweetmeck.ui.home.TimelinePoolProvider
import com.monday8am.tweetmeck.ui.login.AuthViewModel
import com.monday8am.tweetmeck.ui.login.SignInViewModelDelegate
import com.monday8am.tweetmeck.ui.login.SignInViewModelDelegateImpl
import com.monday8am.tweetmeck.ui.onboarding.OnboardingViewModel
import com.monday8am.tweetmeck.ui.tweet.TweetViewModel
import com.monday8am.tweetmeck.ui.user.UserViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single { TwitterDatabase.create(androidContext()) }
    single<SharedPreferencesService> { SharedPreferencesServiceImpl(androidContext()) }
    single<TwitterClient> { TwitterClientImpl(
        BuildConfig.apiKey,
        BuildConfig.apiSecret,
        BuildConfig.accessToken,
        BuildConfig.accessTokenSecret,
        BuildConfig.callbackUrl) }

    factory { get<TwitterDatabase>().tweetDao() }
    factory { get<TwitterDatabase>().twitterListDao() }
    factory { get<TwitterDatabase>().twitterUserDao() }

    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<DataRepository> { DataRepositoryImpl(get(), get()) }
    single<SignInViewModelDelegate> { SignInViewModelDelegateImpl(get()) }

    viewModel { OnboardingViewModel(get()) }
    viewModel { AuthViewModel() }
    viewModel { HomeViewModel(get()) }
    viewModel { (userId: Long) -> UserViewModel(userId, get()) }
    viewModel { (tweetId: Long) -> TweetViewModel(tweetId, get()) }

    scope(named<HomeFragment>()) {
        scoped { TimelinePoolProvider() }
    }
}
