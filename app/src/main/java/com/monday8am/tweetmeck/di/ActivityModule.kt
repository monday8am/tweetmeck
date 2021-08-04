package com.monday8am.tweetmeck.di

import com.monday8am.tweetmeck.util.TimelinePoolProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {

    @ActivityScoped
    @Provides
    fun providesTimelinePoolProvider(): TimelinePoolProvider {
        return TimelinePoolProvider()
    }
}
