package com.monday8am.tweetmeck.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.databinding.FragmentHomeBinding
import com.monday8am.tweetmeck.ui.delegates.AuthState
import com.monday8am.tweetmeck.ui.login.AuthViewModel
import com.monday8am.tweetmeck.ui.timeline.TimelineFragment
import com.monday8am.tweetmeck.util.EventObserver
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class HomeFragment : Fragment() {

    private val tabsCacheSize = 5

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewPager2: ViewPager2

    private val viewModel: HomeViewModel by viewModel()
    private val authViewModel: AuthViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@HomeFragment.viewModel
        }
        viewPager2 = binding.viewpager2
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.twitterLists.observe(viewLifecycleOwner, Observer<List<TwitterList>> { lists ->
            Timber.d("$lists")
            bindContent(view, lists)
        })

        viewModel.navigateToSignInDialog.observe(viewLifecycleOwner, EventObserver { isSigned ->
            findNavController().navigate(
                if (isSigned) {
                    R.id.action_timeline_dest_to_sign_out_dialog_dest
                } else {
                    R.id.action_timeline_dest_to_sign_in_dialog_dest
                }
            )
        })

        authViewModel.authState.observe(viewLifecycleOwner, EventObserver { state ->
            when (state) {
                is AuthState.Loading -> {
                    // viewBinding.button.alpha = 0.5f
                    // viewBinding.button.isEnabled = false
                    Timber.d("Loading")
                }
                is AuthState.WaitingForUserCredentials -> {
                    findNavController().navigate(R.id.action_timeline_dest_to_auth_dest)
                }
                is AuthState.Logged -> {
                    Timber.d("Logged")
                }
                is AuthState.Error -> {
                    Timber.d("Error")
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.dataLoading.observe(viewLifecycleOwner, Observer<Boolean> {
            Timber.d("Loading: $it")
        })

        // Show an error message
        viewModel.errorMessage.observe(this, EventObserver { errorMsg ->
            // TODO: Change once there's a way to show errors to the user
            Toast.makeText(this.context, errorMsg, Toast.LENGTH_LONG).show()
        })
    }

    private fun bindContent(view: View, items: List<TwitterList>) {
        val tabs: TabLayout = view.findViewById(R.id.tabs)

        viewPager2.offscreenPageLimit = tabsCacheSize
        viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return TimelineFragment.newInstance(TimelineQuery.List(items[position].id))
            }

            override fun getItemCount(): Int {
                return items.count()
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

        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {}
        })

        // Attach tabs scrolling to viewPager after its adapter is defined
        TabLayoutMediator(tabs, viewPager2) { tab, position ->
            tab.text = items[position].name
        }.attach()
    }
}
