package com.monday8am.tweetmeck

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import nav.enro.annotations.NavigationComponent
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.navigationController
import nav.enro.core.plugins.EnroLogger
import timber.log.Timber

@HiltAndroidApp
@NavigationComponent
class TweetmeckApplication : Application(), NavigationApplication {

    override val navigationController = navigationController {
        withPlugin(EnroLogger())
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
