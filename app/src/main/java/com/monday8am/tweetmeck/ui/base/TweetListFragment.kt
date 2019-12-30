package com.monday8am.tweetmeck.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.databinding.FragmentTweetListBinding
import com.monday8am.tweetmeck.ui.home.TimelinePoolProvider
import com.monday8am.tweetmeck.util.EventObserver
import org.koin.androidx.scope.currentScope
import timber.log.Timber

abstract class TweetListFragment : Fragment() {

    private lateinit var adapter: TweetListAdapter
    private lateinit var binding: FragmentTweetListBinding
    private val viewPoolProvider: TimelinePoolProvider? by lazy {
        activity?.currentScope?.get<TimelinePoolProvider>()
    }

    private var skeletonScreen: SkeletonScreen? = null

    abstract var viewModel: TweetListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTweetListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@TweetListFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textCreator = TweetItemTextCreator(view.context, viewModel.currentSession)
        adapter = TweetListAdapter(viewModel, viewLifecycleOwner, textCreator)

        binding.recyclerview.addItemDecoration(DividerItemDecoration(
            binding.recyclerview.context,
            DividerItemDecoration.VERTICAL
        ))

        viewModel.pagedList.observe(viewLifecycleOwner, Observer<PagedList<Tweet>> {
            adapter.submitList(it) {
                if (it.isNotEmpty()) {
                    Timber.d("Loading data for ${it.first().listId}: loaded: ${it.loadedCount} placeholders: ${it.size} ")
                }

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
            (layoutManager as LinearLayoutManager).recycleChildrenOnDetach = true
            (itemAnimator as DefaultItemAnimator).run {
                supportsChangeAnimations = false
                addDuration = 160L
                moveDuration = 160L
                changeDuration = 160L
                removeDuration = 120L
            }
        }

        viewModel.loadMoreState.observe(viewLifecycleOwner, Observer {
            Timber.d("Request state! $it")
        })

        viewModel.dataLoading.observe(viewLifecycleOwner, Observer<Boolean> { loading ->
            if (loading) {
                skeletonScreen = Skeleton.bind(binding.recyclerview)
                    .shimmer(false)
                    .load(R.layout.item_tweet_skeleton)
                    .show()
            } else {
                binding.recyclerview.apply {
                    adapter = this@TweetListFragment.adapter
                    setRecycledViewPool(viewPoolProvider?.tweetItemPool)
                }
            }
        })

        // Show an error message
        viewModel.errorMessage.observe(viewLifecycleOwner, EventObserver { errorMsg ->
            // TODO: Change once there's a way to show errors to the user
            Toast.makeText(this.context, errorMsg, Toast.LENGTH_LONG).show()
        })
    }
}
