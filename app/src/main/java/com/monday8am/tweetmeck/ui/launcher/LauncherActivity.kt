/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monday8am.tweetmeck.ui.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.monday8am.tweetmeck.MainActivity
import com.monday8am.tweetmeck.MainKey
import com.monday8am.tweetmeck.ui.onboarding.OnboardingActivity
import com.monday8am.tweetmeck.ui.onboarding.OnnboardingKey
import com.monday8am.tweetmeck.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationAnimations
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.addOpenInstruction
import kotlinx.android.parcel.Parcelize

/**
 * A 'Trampoline' activity for sending users to an appropriate screen on launch.
 */
@Parcelize
class LauncherKey : NavigationKey

@AndroidEntryPoint
@NavigationDestination(OnnboardingKey::class, allowDefault = true)
class LauncherActivity : AppCompatActivity() {

    private val launchViewModel: LaunchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchViewModel.launchDestination.observe(
            this,
            EventObserver { destination ->
                val instruction = NavigationInstruction.Open(
                    navigationDirection = NavigationDirection.REPLACE_ROOT,
                    navigationKey = if (destination == LaunchDestination.MAIN_ACTIVITY) MainKey() else OnnboardingKey(),
                    animations = NavigationAnimations.none
                )
                val intent = Intent(
                    this,
                    if (destination == LaunchDestination.MAIN_ACTIVITY) MainActivity::class.java else OnboardingActivity::class.java
                ).addOpenInstruction(instruction)
                startActivity(intent)
                finish()
            }
        )
    }
}
