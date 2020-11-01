package com.monday8am.tweetmeck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.monday8am.tweetmeck.ui.home.HomeKey
import com.monday8am.tweetmeck.ui.login.AuthenticateKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey
import nav.enro.core.forward
import nav.enro.core.navigationHandle

@Parcelize
class MainKey : NavigationKey

@NavigationDestination(MainKey::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val navigation by navigationHandle<MainKey> {
        container(R.id.homeContainer) {
            it is HomeKey || it is AuthenticateKey
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.forward(HomeKey())
    }
}
