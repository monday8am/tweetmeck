package com.monday8am.tweetmeck.ui.onboarding

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.monday8am.tweetmeck.MainKey
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.databinding.ActivityOnboardingBinding
import com.monday8am.tweetmeck.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationAnimations
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.navigationHandle
import kotlinx.android.parcel.Parcelize

@Parcelize
class OnnboardingKey : NavigationKey

@AndroidEntryPoint
@NavigationDestination(OnnboardingKey::class)
class OnboardingActivity : AppCompatActivity() {

    private val navigation by navigationHandle<OnnboardingKey>()
    private val numberOfPages by lazy { OnBoardingPage.values().size }
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityOnboardingBinding>(
            this, R.layout.activity_onboarding
        ).apply {
            viewModel = onboardingViewModel
            lifecycleOwner = this@OnboardingActivity
        }
        onboardingViewModel.navigateToMainActivity.observe(
            this,
            EventObserver {
                this.run {
                    navigation.executeInstruction(
                        NavigationInstruction.Open(
                            navigationDirection = NavigationDirection.REPLACE_ROOT,
                            navigationKey = MainKey(),
                            animations = NavigationAnimations.default
                        )
                    )
                }
            }
        )

        bindContent(binding)
        setImmersiveMode()
        setupTransition(binding)
    }

    private fun setImmersiveMode() {
        // immersive mode so images can draw behind the status bar
        val decor = window.decorView
        val flags = decor.systemUiVisibility or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        decor.systemUiVisibility = flags
    }

    private fun bindContent(binding: ActivityOnboardingBinding) {
        with(binding.slider) {
            adapter = OnBoardingPagerAdapter()
            setPageTransformer { page, position ->
                // setParallaxTransformation(page, position)
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    if (numberOfPages > 1) {
                        val newProgress = (position + positionOffset) / (numberOfPages - 1)
                        binding.onboardingRoot.progress = newProgress
                    }
                }
            })

            binding.pageIndicator.setViewPager2(this)
        }

        binding.nextBtn.setOnClickListener {
            val nextSlidePos: Int = binding.slider.currentItem.plus(1)
            binding.slider.setCurrentItem(nextSlidePos, true)
        }

        binding.tagButtonsTv.movementMethod = LinkMovementMethod.getInstance()
        binding.tagButtonsTv.linksClickable = true
        binding.tagButtonsTv.text = getTagsContent()
    }

    private fun getTagsContent(): CharSequence {
        val links = TimelineTopics.values().map {
            when (it) {
                TimelineTopics.NEWS -> getString(R.string.tag_news_btn)
                TimelineTopics.POLITICS -> getString(R.string.tag_politics_btn)
                TimelineTopics.SPORTS -> getString(R.string.tag_sports_btn)
                TimelineTopics.SCIENCE -> getString(R.string.tag_science_btn)
                TimelineTopics.TECH -> getString(R.string.tag_tech_btn)
            }
        }
        val spannable = SpannableStringBuilder(links.joinToString(" "))
        val clickableSpans = TimelineTopics.values().map {
            getClickableSpan(
                action = onboardingViewModel::getStartedClick,
                value = it
            )
        }

        var offSet = 0
        links.forEachIndexed { index, content ->
            spannable.setSpan(clickableSpans[index], offSet, offSet + content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            offSet += content.length + 1
        }
        return spannable
    }

    private fun getClickableSpan(
        action: (TimelineTopics) -> Unit,
        value: TimelineTopics,
        isBold: Boolean = false
    ): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) = action.invoke(value)
            override fun updateDrawState(ds: TextPaint) {
                ds.isFakeBoldText = isBold
                ds.isUnderlineText = true
            }
        }
    }

    private fun setupTransition(binding: ActivityOnboardingBinding) {
        // Transition the logo animation (roughly) from the preview window background.
        /*
        binding.logo.apply {
            val interpolator =
                AnimationUtils.loadInterpolator(context, interpolator.linear_out_slow_in)
            alpha = 0.4f
            scaleX = 0.8f
            scaleY = 0.8f
            doOnNextLayout {
                translationY = height / 3f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(interpolator)
                    .withEndAction {
                        postDelayed(1000) {
                            (binding.logo.drawable as AnimatedVectorDrawable).start()
                        }
                    }
            }
        }
         */
    }
}
