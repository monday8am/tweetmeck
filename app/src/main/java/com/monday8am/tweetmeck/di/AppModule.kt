package com.monday8am.tweetmeck.di

import android.content.Context
import com.monday8am.tweetmeck.BuildConfig
import com.monday8am.tweetmeck.data.local.PreferenceStorage
import com.monday8am.tweetmeck.data.local.SharedPreferencesServiceImpl
import com.monday8am.tweetmeck.data.local.TweetDao
import com.monday8am.tweetmeck.data.local.TwitterDatabase
import com.monday8am.tweetmeck.data.local.TwitterListDao
import com.monday8am.tweetmeck.data.local.TwitterUserDao
import com.monday8am.tweetmeck.data.remote.TwitterClient
import com.monday8am.tweetmeck.data.remote.TwitterClientImpl
import com.monday8am.tweetmeck.domain.auth.GetAuthUrlUseCase
import com.monday8am.tweetmeck.domain.auth.ObserveLoggedSessionUseCase
import com.monday8am.tweetmeck.domain.auth.SignInUseCase
import com.monday8am.tweetmeck.domain.auth.SignOutUseCase
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegate
import com.monday8am.tweetmeck.ui.delegates.SignInViewModelDelegateImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun providesTwitterDb(@ApplicationContext context: Context): TwitterDatabase {
        return TwitterDatabase.create(context)
    }

    @Singleton
    @Provides
    fun providesTwitterClient(): TwitterClient {
        return TwitterClientImpl(
            BuildConfig.apiKey,
            BuildConfig.apiSecret,
            BuildConfig.accessToken,
            BuildConfig.accessTokenSecret,
            BuildConfig.callbackUrl
        )
    }

    @Singleton
    @Provides
    fun providesPreferencesStorage(@ApplicationContext context: Context): PreferenceStorage {
        return SharedPreferencesServiceImpl(context)
    }

    @Singleton
    @Provides
    fun provideSignInViewModelDelegate(
        observeCurrentSessionUseCase: ObserveLoggedSessionUseCase,
        getAuthUrlUseCase: GetAuthUrlUseCase,
        signInUseCase: SignInUseCase,
        signOutUseCase: SignOutUseCase
    ): SignInViewModelDelegate {
        return SignInViewModelDelegateImpl(
            observeCurrentSessionUseCase = observeCurrentSessionUseCase,
            getAuthUrlUseCase = getAuthUrlUseCase,
            signInUseCase = signInUseCase,
            signOutUseCase = signOutUseCase
        )
    }

    @Provides
    fun providesTweetDao(database: TwitterDatabase): TweetDao {
        return database.tweetDao()
    }

    @Provides
    fun providesTwitterListDao(database: TwitterDatabase): TwitterListDao {
        return database.twitterListDao()
    }

    @Provides
    fun providesTwitterUserDao(database: TwitterDatabase): TwitterUserDao {
        return database.twitterUserDao()
    }
}
