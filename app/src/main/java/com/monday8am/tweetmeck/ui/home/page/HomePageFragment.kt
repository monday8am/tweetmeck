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

class HomePageFragment : Fragment() {

    companion object {
        private const val ARG_QUERY_ID = "arg.QUERY_ID"

        @JvmStatic
        fun newInstance(query: TimelineQuery) =
            HomePageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUERY_ID, query.toFormattedString())
                }
            }
    }

    private val query: TimelineQuery by lazyFast {
        val queryString = arguments?.getString(ARG_QUERY_ID) ?: throw IllegalStateException("Missing arguments!")
        TimelineQuery.fromFormattedString(queryString)
    }

    private val viewPoolProvider: TimelinePoolProvider? by lazy {
        activity?.currentScope?.get<TimelinePoolProvider>()
    }

    private lateinit var binding: FragmentHomePageBinding
    private lateinit var viewModel: HomePageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel(from = { requireParentFragment() })
        binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.scrollToTop.observe(viewLifecycleOwner, EventObserver {
            binding.timelineView.scrollToTop()
        })

        viewModel.timelineContent.observe(viewLifecycleOwner, Observer {
            binding.timelineView.bind(viewModel, viewLifecycleOwner, viewPoolProvider)
        })

        viewModel.refreshTimelineContent(query)
    }
}
