package com.monday8am.tweetmeck.ui.home.page

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.ui.timeline.TimelineFragment
import com.monday8am.tweetmeck.util.lazyFast
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import timber.log.Timber

class HomePageFragment : TimelineFragment() {

    companion object {
        private const val ARG_QUERY_ID = "arg.QUERY_ID"
        private const val ARG_INDEX_ID = "arg.INDEX_ID"

        @JvmStatic
        fun newInstance(query: TimelineQuery, index: Int) =
            newInstance(query).apply {
                arguments = Bundle().apply {
                    putInt(ARG_INDEX_ID, index)
                }
            }
    }

    private val query: TimelineQuery by lazyFast {
        val queryString =
            arguments?.getString(ARG_QUERY_ID) ?: throw IllegalStateException("Missing arguments!")
        TimelineQuery.fromFormattedString(queryString)
    }

    private val position: Int by lazyFast {
        arguments?.getInt(ARG_INDEX_ID) ?: -1
    }

    private lateinit var pageViewModel: HomePageViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageViewModel = getSharedViewModel(from = { requireParentFragment() })
        pageViewModel.scrollToTop.observe(viewLifecycleOwner, Observer {
            Timber.d("Selected position $it")
            if (it == position) {
                // binding.homeTimelineView.scrollToTop()
            }
        })
    }
}
