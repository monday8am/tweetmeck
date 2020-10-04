package com.monday8am.tweetmeck.di

import com.monday8am.tweetmeck.data.local.PreferenceStorage
import com.monday8am.tweetmeck.data.local.SharedPreferencesServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

/*
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
    viewModel { HomePageViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { (screenName: String) -> UserViewModel(screenName, get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (tweetId: Long) -> TweetViewModel(tweetId, get()) }
    viewModel { SearchViewModel(get(), get(), get(), get(), get(), get()) }

    scope(named<HomeFragment>()) {
        scoped { TimelinePoolProvider() }
    }
}
*/

@Module
@InstallIn(ApplicationComponent::class)
abstract class AppModule {

    @Singleton
    @Binds
    abstract fun bindPreferencesStorage(preferenceStorage: SharedPreferencesServiceImpl): PreferenceStorage
}
