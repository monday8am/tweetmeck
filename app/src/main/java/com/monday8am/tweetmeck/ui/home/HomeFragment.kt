package com.monday8am.tweetmeck.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.databinding.FragmentHomeBinding
import com.monday8am.tweetmeck.ui.delegates.AuthState
import com.monday8am.tweetmeck.ui.home.page.HomePageFragment
import com.monday8am.tweetmeck.ui.home.page.HomePageViewModel
import com.monday8am.tweetmeck.ui.login.AuthenticateKey
import com.monday8am.tweetmeck.ui.login.SignInKey
import com.monday8am.tweetmeck.ui.login.SignOutKey
import com.monday8am.tweetmeck.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import nav.enro.annotations.NavigationDestination
import nav.enro.core.NavigationKey
import nav.enro.core.forward
import nav.enro.core.navigationHandle

@Parcelize
class HomeKey : NavigationKey

@NavigationDestination(HomeKey::class)
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val tabsCacheSize = 5
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewPager2: ViewPager2

    private val navigation by navigationHandle<HomeKey>()
    private val viewModel: HomeViewModel by activityViewModels()
    private val pageViewModel: HomePageViewModel by viewModels()

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

        viewModel.twitterLists.observe(viewLifecycleOwner, { lists ->
            bindContent(view, lists)
        })

        viewModel.navigateToSignInDialog.observe(viewLifecycleOwner, EventObserver { isSigned ->
            val key = if (isSigned) SignOutKey() else SignInKey()
            navigation.forward(key)
        })

        viewModel.authState.observe(viewLifecycleOwner, EventObserver { state ->
            when (state) {
                is AuthState.Loading -> { }
                is AuthState.WaitingForUserCredentials -> {
                    navigation.forward(AuthenticateKey())
                }
                is AuthState.Logged -> { }
                is AuthState.Error -> { }
                else -> { }
            }
        })

        viewModel.dataLoading.observe(viewLifecycleOwner, {
        })

        // Show an error message
        viewModel.errorMessage.observe(viewLifecycleOwner, EventObserver { errorMsg ->
            Toast.makeText(this.context, errorMsg, Toast.LENGTH_LONG).show()
        })

        pageViewModel.openUrl.observe(viewLifecycleOwner, EventObserver {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
        })

        pageViewModel.navigateToTweetDetails.observe(viewLifecycleOwner, EventObserver { tweetId ->
            // findNavController().navigate(HomeFragmentDirections.actionHomeToTweet(tweetId))
        })

        pageViewModel.navigateToUserDetails.observe(viewLifecycleOwner, EventObserver { screenName ->
            // findNavController().navigate(HomeFragmentDirections.actionHomeToUser(screenName))
        })

        pageViewModel.navigateToSearch.observe(viewLifecycleOwner, EventObserver { searchItem ->
            // findNavController().navigate(HomeFragmentDirections.actionHomeToSearch(searchItem))
        })
    }

    private fun bindContent(view: View, items: List<TwitterList>) {
        val tabs: TabLayout = view.findViewById(R.id.tabs)

        viewPager2.offscreenPageLimit = tabsCacheSize
        viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return HomePageFragment.newInstance(TimelineQuery.List(items[position].id), position)
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

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.position?.let {
                    pageViewModel.setScrollToTop(it)
                }
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
