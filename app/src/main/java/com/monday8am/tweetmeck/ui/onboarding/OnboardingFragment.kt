package com.monday8am.tweetmeck.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.databinding.OnboardingFragmentBinding
import com.monday8am.tweetmeck.util.EventObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var viewBinding: OnboardingFragmentBinding
    private val viewModel: OnboardingViewModel by viewModel()

    private lateinit var viewPager2: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = OnboardingFragmentBinding.inflate(layoutInflater)
        viewPager2 = viewBinding.onboardingViewpager2
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.onboardingPresented.observe(viewLifecycleOwner, EventObserver { isPresented ->
            if (isPresented) {
                navigateToTimeline()
            } else {
                viewPager2.visibility = View.VISIBLE
            }
        })

        viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                when(position) {
                    
                }
                return TimelineFragment.newInstance(items[position].id)
            }

            override fun getItemCount(): Int {
                return items.count()
            }

            override fun onViewDetachedFromWindow(holder: FragmentViewHolder) {
                super.onViewDetachedFromWindow(holder)
                holder.adapterPosition
            }
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (items.size > position) {
                    viewModel.onChangedDisplayedTimeline(items[position].id)
                }
            }
        })
    }

    private fun navigateToTimeline() = findNavController().navigate(R.id.action_onboarding_dest_to_timeline_dest)
}
