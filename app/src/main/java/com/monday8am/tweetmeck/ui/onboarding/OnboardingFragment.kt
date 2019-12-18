package com.monday8am.tweetmeck.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.databinding.OnboardingFragmentBinding
import com.monday8am.tweetmeck.util.EventObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var viewBinding: OnboardingFragmentBinding
    private val viewModel: OnboardingViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = OnboardingFragmentBinding.inflate(layoutInflater)
        viewBinding.button.setOnClickListener {
            viewModel.saveOnboardingPresented()
            viewBinding.button.alpha = 0.5f
            viewBinding.button.isEnabled = false
        }
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.onboardingPresented.observe(viewLifecycleOwner, EventObserver { isPresented ->
            if (isPresented) {
                navigateToTimeline()
            } else {
                viewBinding.onboardingGroup.visibility = View.VISIBLE
            }
        })
    }

    private fun navigateToTimeline() = findNavController().navigate(R.id.action_onboarding_dest_to_timeline_dest)
}
