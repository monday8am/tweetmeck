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
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.databinding.HomeFragmentBinding
import com.monday8am.tweetmeck.ui.dialogs.SignInDialogDispatcher
import com.monday8am.tweetmeck.ui.home.timeline.TimelineFragment
import com.monday8am.tweetmeck.ui.login.AuthState
import com.monday8am.tweetmeck.ui.login.AuthViewModel
import com.monday8am.tweetmeck.util.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class HomeFragment : Fragment() {

    private lateinit var binding: HomeFragmentBinding
    private lateinit var viewPager2: ViewPager2

    private val viewModel: HomeViewModel by viewModel()
    private val authViewModel: AuthViewModel by sharedViewModel()
    private val dialogDispatcher: SignInDialogDispatcher by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@HomeFragment.viewModel
        }
        viewPager2 = binding.viewpager2
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.twitterLists.observe(viewLifecycleOwner, Observer<List<TwitterList>> { lists ->
            bindContent(view, lists)
        })

        viewModel.navigateToTweetDetails.observe(viewLifecycleOwner, EventObserver { tweetId ->
            this.findNavController().navigate(HomeFragmentDirections.actionTimelineDestToTweetDest(tweetId))
        })

        viewModel.navigateToUserDetails.observe(viewLifecycleOwner, EventObserver { userId ->
            this.findNavController().navigate(HomeFragmentDirections.actionTimelineDestToUserDest(userId))
        })

        viewModel.navigateToSignInDialog.observe(viewLifecycleOwner, EventObserver { isSigned ->
            if (isSigned) {
                this.findNavController().navigate(R.id.action_timeline_dest_to_sign_in_dialog_dest)
            } else {
                dialogDispatcher.openSignInDialog(requireActivity())
            }
        })

        authViewModel.authState.observe(viewLifecycleOwner, Observer<AuthState> { state ->
            when (state) {
                is AuthState.Loading -> {
                    // viewBinding.button.alpha = 0.5f
                    // viewBinding.button.isEnabled = false
                    Timber.d("Loading")
                }
                is AuthState.WaitingForUserCredentials -> this.findNavController().navigate(R.id.action_timeline_dest_to_auth_dest)
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
        val appbar: View = view.findViewById(R.id.appbar)
        val tabs: TabLayout = view.findViewById(R.id.tabs)

        viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return TimelineFragment.newInstance(items[position].id)
            }

            override fun getItemCount(): Int {
                return items.count()
            }
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.onChangedDisplayedTimeline(items[position].id)
            }
        })

        // Attach tabs scrolling to viewPager after its adapter is defined
        TabLayoutMediator(tabs, viewPager2) { tab, position ->
            tab.text = items[position].name
        }.attach()
    }
}
