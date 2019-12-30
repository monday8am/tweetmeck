package com.monday8am.tweetmeck.ui.home.timeline

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.monday8am.tweetmeck.ui.base.TweetListFragment
import com.monday8am.tweetmeck.ui.base.TweetListViewModel
import com.monday8am.tweetmeck.ui.home.HomeFragmentDirections
import com.monday8am.tweetmeck.util.EventObserver
import com.monday8am.tweetmeck.util.lazyFast
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class TimelineFragment : TweetListFragment() {

    companion object {
        private const val ARG_LIST_ID = "arg.TWEET_LIST_ID"

        @JvmStatic
        fun newInstance(listId: Long) =
            TimelineFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_LIST_ID, listId)
                }
            }
    }

    private val listId: Long by lazyFast {
        val args = arguments ?: throw IllegalStateException("Missing arguments!")
        args.getLong(ARG_LIST_ID)
    }
    override var viewModel: TweetListViewModel
        get() = getViewModel<TimelineViewModel> { parametersOf(listId) }
        set(value) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateToTweetDetails.observe(viewLifecycleOwner, EventObserver { tweetId ->
            Timber.d("Tweet id $tweetId")
            findNavController().navigate(HomeFragmentDirections.actionTimelineDestToTweetDest(tweetId))
        })

        viewModel.navigateToUserDetails.observe(viewLifecycleOwner, EventObserver { userId ->
            Timber.d("User id $userId")
            findNavController().navigate(HomeFragmentDirections.actionTimelineDestToUserDest(userId))
        })
    }
}
