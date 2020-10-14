package com.monday8am.tweetmeck.ui.navigator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.ui.home.HomeFragment
import com.ncapdevi.fragnav.FragNavController

interface ScreenNavigator {
    fun initialize(bundle: Bundle?)
    fun toHome()
    fun navigateBack(): Boolean
    fun saveInstanceState(bundle: Bundle)
}

class ScreenNavigatorImp(activity: AppCompatActivity) :
    FragNavController.RootFragmentListener,
    ScreenNavigator {

    private var fragNavController: FragNavController = FragNavController(activity.supportFragmentManager, R.id.drawer_layout)

    init {
        fragNavController.rootFragmentListener = this
    }

    override fun initialize(bundle: Bundle?) {
        fragNavController.initialize(0, bundle)
    }

    override fun toHome() {
        fragNavController.clearStack()
        fragNavController.pushFragment(HomeFragment())
    }

    fun toTweetDetails() {
        // mFragNavController.pushFragment()
    }

    override fun navigateBack(): Boolean {
        return if (fragNavController.isRootFragment) {
            false
        } else {
            fragNavController.popFragment()
            true
        }
    }

    override fun saveInstanceState(bundle: Bundle) {
        fragNavController.onSaveInstanceState(bundle)
    }

    override val numberOfRootFragments: Int = 1

    override fun getRootFragment(index: Int): Fragment {
        return HomeFragment()
    }
}
