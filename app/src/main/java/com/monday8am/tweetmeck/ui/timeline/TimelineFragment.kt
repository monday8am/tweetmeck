package com.monday8am.tweetmeck.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.databinding.FragmentTimelineBinding
import com.monday8am.tweetmeck.ui.home.HomeViewModel
import com.monday8am.tweetmeck.ui.home.TimelinePoolProvider
import com.monday8am.tweetmeck.util.EventObserver
import com.monday8am.tweetmeck.util.lazyFast
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class TimelineFragment : Fragment() {

    companion object {
        private const val ARG_QUERY_ID = "arg.QUERY_ID"

        @JvmStatic
        fun newInstance(query: TimelineQuery) =
            TimelineFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUERY_ID, query.toFormattedString())
                }
            }
    }

    private val query: TimelineQuery by lazyFast {
        val queryString = arguments?.getString(ARG_QUERY_ID) ?: throw IllegalStateException("Missing arguments!")
        TimelineQuery.fromFormattedString(queryString)
    }

    private lateinit var adapter: TimelineAdapter
    private lateinit var binding: FragmentTimelineBinding

    private val viewPoolProvider: TimelinePoolProvider? by lazy {
        activity?.currentScope?.get<TimelinePoolProvider>()
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel(from = { requireParentFragment() })
        binding = FragmentTimelineBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@TimelineFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textCreator = TweetItemTextCreator(view.context, viewModel.lastSession)
        adapter = TimelineAdapter(viewModel, viewLifecycleOwner, textCreator)

        binding.recyclerview.addItemDecoration(DividerItemDecoration(
            binding.recyclerview.context,
            DividerItemDecoration.VERTICAL
        ))

        viewModel.getTimelineContent(query).pagedList.observe(viewLifecycleOwner, Observer<PagedList<Tweet>> {
            adapter.submitList(it) {
                // Workaround for an issue where RecyclerView incorrectly uses the loading / spinner
                // item added to the end of the list as an anchor during initial load.
                val layoutManager = (binding.recyclerview.layoutManager as LinearLayoutManager)
                val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (position != RecyclerView.NO_POSITION) {
                    binding.recyclerview.scrollToPosition(position)
                }
            }
        })

        viewModel.scrollToTop.observe(viewLifecycleOwner, EventObserver {
            binding.recyclerview.scrollToPosition(0)
        })

        binding.recyclerview.apply {
            adapter = this@TimelineFragment.adapter
            setRecycledViewPool(viewPoolProvider?.tweetItemPool)
            (layoutManager as LinearLayoutManager).recycleChildrenOnDetach = true
            (itemAnimator as DefaultItemAnimator).run {
                supportsChangeAnimations = false
                addDuration = 160L
                moveDuration = 160L
                changeDuration = 160L
                removeDuration = 120L
            }
        }
    }
}
