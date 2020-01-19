package com.monday8am.tweetmeck

import android.app.Application
import com.monday8am.tweetmeck.di.appModule
import com.monday8am.tweetmeck.di.useCasesModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class TweetmeckApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        startKoin {
            androidLogger()
            androidContext(this@TweetmeckApplication)
            modules(listOf(appModule, useCasesModule))
        }
    }
}
