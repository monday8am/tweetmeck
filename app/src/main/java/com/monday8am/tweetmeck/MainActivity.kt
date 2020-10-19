package com.monday8am.tweetmeck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.monday8am.tweetmeck.ui.home.HomeNavKey
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey
import nav.enro.core.navigationHandle
import timber.log.Timber

@Parcelize
class MainKey : NavigationKey

@NavigationDestination(MainKey::class)
class MainActivity : AppCompatActivity() {

    private val navigation by navigationHandle<MainKey> {
        container(R.id.homeContainer) {
            it is HomeNavKey
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.d("Navigation key: ${navigation.key}")
    }
}
