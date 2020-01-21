package com.monday8am.tweetmeck.ui.home.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.databinding.FragmentHomePageBinding
import com.monday8am.tweetmeck.util.EventObserver
import com.monday8am.tweetmeck.util.TimelinePoolProvider
import com.monday8am.tweetmeck.util.lazyFast
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import timber.log.Timber

class HomePageFragment : Fragment() {

    companion object {
        private const val ARG_QUERY_ID = "arg.QUERY_ID"
        private const val ARG_INDEX_ID = "arg.INDEX_ID"

        @JvmStatic
        fun newInstance(query: TimelineQuery, index: Int) =
            HomePageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUERY_ID, query.toFormattedString())
                    putInt(ARG_INDEX_ID, index)
                }
            }
    }

    private val query: TimelineQuery by lazyFast {
        val queryString = arguments?.getString(ARG_QUERY_ID) ?: throw IllegalStateException("Missing arguments!")
        TimelineQuery.fromFormattedString(queryString)
    }

    private val position: Int by lazyFast {
        arguments?.getInt(ARG_INDEX_ID) ?: -1
    }

    private val viewPoolProvider: TimelinePoolProvider? by lazy {
        parentFragment?.currentScope?.get<TimelinePoolProvider>()
    }

    private lateinit var binding: FragmentHomePageBinding
    private lateinit var pageViewModel: HomePageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        pageViewModel = getSharedViewModel(from = { requireParentFragment() })
        binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageViewModel.scrollToTop.observe(viewLifecycleOwner, Observer {
            Timber.d("Selected position $it")
            if(it == position) {
                binding.homeTimelineView.scrollToTop()
            }
        })

        pageViewModel.timelineContent.observe(viewLifecycleOwner, Observer {
            val (timelineQuery, timelineContent) = it
            if (query == timelineQuery) {
                Timber.d("Try to bind from fragment! ${query.toFormattedString()}")
                binding.homeTimelineView.bind(
                    timelineContent,
                    pageViewModel,
                    viewLifecycleOwner,
                    viewPoolProvider
                )
            }
        })

        pageViewModel.refreshTimelineContent(query)
    }
}
