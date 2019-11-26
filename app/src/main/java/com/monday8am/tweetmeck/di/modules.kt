package com.monday8am.tweetmeck.di

import androidx.recyclerview.widget.RecyclerView
import com.monday8am.tweetmeck.BuildConfig
import com.monday8am.tweetmeck.data.AuthRepository
import com.monday8am.tweetmeck.data.AuthRepositoryImpl
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.data.DataRepositoryImpl
import com.monday8am.tweetmeck.data.local.*
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.data.remote.TwitterClientImpl
import com.monday8am.tweetmeck.login.AuthViewModel
import com.monday8am.tweetmeck.timeline.TimelineFragment
import com.monday8am.tweetmeck.timeline.TimelineViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single { TwitterDatabase.create(androidContext()) }
    single<SharedPreferencesService> { SharedPreferencesServiceImpl(androidContext()) }
    single<TwitterClient> { TwitterClientImpl(BuildConfig.apiKey, BuildConfig.apiSecret, get(), BuildConfig.callbackUrl)}

    factory { get<TwitterDatabase>().tweetDao() }
    factory { get<TwitterDatabase>().twitterListDao() }
    factory { get<TwitterDatabase>().twitterUserDao() }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<DataRepository> { DataRepositoryImpl(get(), get()) }

    viewModel { AuthViewModel(get()) }
    viewModel { TimelineViewModel(get()) }

    scope(named<TimelineFragment>()) {
        scoped { RecyclerView.RecycledViewPool() }
    }
}
