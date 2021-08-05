package com.monday8am.tweetmeck

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.enro.annotations.NavigationComponent
import dev.enro.core.controller.NavigationApplication
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.navigationController
import dev.enro.core.plugins.EnroLogger
import timber.log.Timber

@HiltAndroidApp
@NavigationComponent
class TweetmeckApplication : Application(), NavigationApplication {

    override val navigationController: NavigationController = navigationController {
        plugin(EnroLogger())
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
