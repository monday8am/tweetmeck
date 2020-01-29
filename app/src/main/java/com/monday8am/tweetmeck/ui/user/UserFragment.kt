package com.monday8am.tweetmeck.ui.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.monday8am.tweetmeck.databinding.FragmentUserBinding
import com.monday8am.tweetmeck.util.EventObserver
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class UserFragment : Fragment() {

    private val navArgs: UserFragmentArgs by navArgs()
    private lateinit var userViewModel: UserViewModel
    private lateinit var binding: FragmentUserBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userViewModel = getViewModel { parametersOf(navArgs.screenName) }

        binding = FragmentUserBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = this@UserFragment.userViewModel
            user = userViewModel.user
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel.timelineContent.observe(viewLifecycleOwner, Observer {
            val (_, timelineContent) = it
            binding.userTimelineView.bind(
                timelineContent,
                userViewModel,
                viewLifecycleOwner
            )
        })

        userViewModel.openUrl.observe(viewLifecycleOwner, EventObserver {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
        })

        userViewModel.navigateToTweetDetails.observe(viewLifecycleOwner, EventObserver { tweetId ->
            findNavController().navigate(UserFragmentDirections.actionUserToTweet(tweetId))
        })

        userViewModel.navigateToUserDetails.observe(viewLifecycleOwner, EventObserver { screenName ->
            Timber.d(screenName)
        })

        userViewModel.navigateToSearch.observe(viewLifecycleOwner, EventObserver { searchItem ->
            findNavController().navigate(UserFragmentDirections.actionUserToSearch(searchItem))
        })
    }
}
