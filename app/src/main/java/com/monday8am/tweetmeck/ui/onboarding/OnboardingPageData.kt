package com.monday8am.tweetmeck.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.monday8am.tweetmeck.R

enum class OnBoardingPage(
    @StringRes val titleResource: Int,
    @StringRes val subTitleResource: Int,
    @DrawableRes val centerImageResource: Int
) {
    ONE(
        R.string.onboarding_slide0_title,
        R.string.onboarding_slide0_subtitle,
        R.drawable.onboarding_step0
    ),
    TWO(
        R.string.onboarding_slide1_title,
        R.string.onboarding_slide1_subtitle,
        R.drawable.onboarding_step1
    ),
    THREE(
        R.string.onboarding_slide2_title,
        R.string.onboarding_slide2_subtitle,
        R.drawable.onboarding_step2
    )
}
