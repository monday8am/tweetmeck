package com.monday8am.tweetmeck.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.ui.timeline.TimelineFragment
import com.monday8am.tweetmeck.util.lazyFast
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomePageFragment : TimelineFragment() {

    companion object {
        private const val ARG_INDEX_ID = "arg.INDEX_ID"

        @JvmStatic
        fun newInstance(query: TimelineQuery, index: Int) =
            newInstance(query).apply {
                arguments = Bundle().apply {
                    putInt(ARG_INDEX_ID, index)
                }
            }
    }

    private val sharedViewModel: HomeViewModel by activityViewModels()

    private val position: Int by lazyFast {
        arguments?.getInt(ARG_INDEX_ID) ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.scrollToTop.observe(
            viewLifecycleOwner,
            {
                Timber.d("Selected position $it")
                if (it == position) {
                    // binding.homeTimelineView.scrollToTop()
                }
            }
        )
    }
}
