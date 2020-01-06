package com.monday8am.tweetmeck.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.monday8am.tweetmeck.MainActivity
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.databinding.ActivityOnboardingBinding
import com.monday8am.tweetmeck.util.EventObserver
import org.koin.android.ext.android.inject

class OnboardingActivity : AppCompatActivity() {

    private val numberOfPages by lazy { OnBoardingPage.values().size }
    private val onboardingViewModel: OnboardingViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityOnboardingBinding>(
            this, R.layout.activity_onboarding
        ).apply {
            viewModel = onboardingViewModel
            lifecycleOwner = this@OnboardingActivity
        }
        onboardingViewModel.navigateToMainActivity.observe(this, EventObserver {
            this.run {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })

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

    private fun bindContent(binding: ActivityOnboardingBinding)  {
        with(binding.slider) {
            adapter = OnBoardingPagerAdapter()
            setPageTransformer { page, position ->
                //setParallaxTransformation(page, position)
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
            binding.nextBtn.setOnClickListener {
                val nextSlidePos: Int = binding.slider.currentItem.plus(1)
                binding.slider.setCurrentItem(nextSlidePos, true)
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
