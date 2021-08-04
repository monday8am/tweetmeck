package com.monday8am.tweetmeck.ui.timeline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.databinding.FragmentTimelineBinding
import com.monday8am.tweetmeck.util.EventObserver
import com.monday8am.tweetmeck.util.TimelinePoolProvider
import com.monday8am.tweetmeck.util.lazyFast
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
open class TimelineFragment : Fragment() {

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

    protected val query: TimelineQuery by lazyFast {
        val queryString = arguments?.getString(ARG_QUERY_ID) ?: throw IllegalStateException("Missing arguments!")
        TimelineQuery.fromFormattedString(queryString)
    }

    private lateinit var adapter: TimelineAdapter
    private lateinit var binding: FragmentTimelineBinding

    @Inject
    lateinit var itemPoolPoint: TimelinePoolProvider

    private val viewModel: TimelineViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimelineBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@TimelineFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textCreator = TweetItemTextCreator(view.context, viewModel.currentSession)
        adapter = TimelineAdapter(viewModel, viewLifecycleOwner, textCreator)

        binding.recyclerview.addItemDecoration(
            DividerItemDecoration(
                binding.recyclerview.context,
                DividerItemDecoration.VERTICAL
            )
        )

        viewModel.pagedList.observe(
            viewLifecycleOwner,
            {
                adapter.submitList(it) {
                    // Workaround for an issue where RecyclerView incorrectly uses the loading / spinner
                    // item added to the end of the list as an anchor during initial load.
                    val layoutManager = (binding.recyclerview.layoutManager as LinearLayoutManager)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        binding.recyclerview.scrollToPosition(position)
                    }
                }
            }
        )

        binding.recyclerview.apply {
            adapter = this@TimelineFragment.adapter
            setRecycledViewPool(itemPoolPoint.tweetItemPool)
            (layoutManager as LinearLayoutManager).recycleChildrenOnDetach = true
            (itemAnimator as DefaultItemAnimator).run {
                supportsChangeAnimations = false
                addDuration = 160L
                moveDuration = 160L
                changeDuration = 160L
                removeDuration = 120L
            }
        }

        viewModel.openUrl.observe(
            viewLifecycleOwner,
            EventObserver {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
            }
        )

        viewModel.loadMoreState.observe(
            viewLifecycleOwner,
            {
                Timber.d("Request state! $it")
            }
        )

        viewModel.navigateToTweetDetails.observe(
            viewLifecycleOwner,
            EventObserver { tweetId ->
                Timber.d("navigate to tweet $tweetId")
            }
        )

        viewModel.navigateToUserDetails.observe(
            viewLifecycleOwner,
            EventObserver { screenName ->
                Timber.d("navigate to user $screenName")
            }
        )

        viewModel.navigateToSearch.observe(
            viewLifecycleOwner,
            EventObserver { searchItem ->
                Timber.d("search for $searchItem")
            }
        )

        // Show an error message
        viewModel.timelineErrorMessage.observe(
            viewLifecycleOwner,
            EventObserver { errorMsg ->
                // TODO: Change once there's a way to show errors to the user
                Toast.makeText(this.context, errorMsg, Toast.LENGTH_LONG).show()
            }
        )

        viewModel.runQuery(query)
    }
}
