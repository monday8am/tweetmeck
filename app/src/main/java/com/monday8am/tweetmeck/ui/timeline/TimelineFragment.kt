package com.monday8am.tweetmeck.ui.timeline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monday8am.tweetmeck.MainNavDirections
import com.monday8am.tweetmeck.data.TimelineQuery
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.databinding.FragmentTimelineBinding
import com.monday8am.tweetmeck.ui.home.HomeFragmentDirections
import com.monday8am.tweetmeck.ui.home.TimelinePoolProvider
import com.monday8am.tweetmeck.util.EventObserver
import com.monday8am.tweetmeck.util.lazyFast
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

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

    private lateinit var viewModel: TimelineViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getViewModel { parametersOf(query) }
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

        binding.recyclerview.addItemDecoration(DividerItemDecoration(
            binding.recyclerview.context,
            DividerItemDecoration.VERTICAL
        ))

        viewModel.pagedList.observe(viewLifecycleOwner, Observer<PagedList<Tweet>> {
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

        viewModel.openUrl.observe(viewLifecycleOwner, EventObserver {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
        })

        viewModel.loadMoreState.observe(viewLifecycleOwner, Observer {
            Timber.d("Request state! $it")
        })

        viewModel.navigateToTweetDetails.observe(viewLifecycleOwner, EventObserver { tweetId ->
            findNavController().navigate(MainNavDirections.actionGlobalTweetAction(tweetId))
        })

        viewModel.navigateToUserDetails.observe(viewLifecycleOwner, EventObserver { screenName ->
            findNavController().navigate(HomeFragmentDirections.actionGlobalUserAction(screenName))
        })

        viewModel.navigateToSearch.observe(viewLifecycleOwner, EventObserver { searchItem ->
            findNavController().navigate(HomeFragmentDirections.actionGlobalSearchAction(searchItem))
        })

        // Show an error message
        viewModel.errorMessage.observe(viewLifecycleOwner, EventObserver { errorMsg ->
            // TODO: Change once there's a way to show errors to the user
            Toast.makeText(this.context, errorMsg, Toast.LENGTH_LONG).show()
        })
    }
}
