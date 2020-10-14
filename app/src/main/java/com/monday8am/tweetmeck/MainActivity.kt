package com.monday8am.tweetmeck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.monday8am.tweetmeck.ui.navigator.ScreenNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var screenNavigator: ScreenNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        screenNavigator.initialize(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        screenNavigator.saveInstanceState(outState)
    }
}
