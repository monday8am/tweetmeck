package com.monday8am.tweetmeck.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.databinding.FragmentTweetListBinding
import com.monday8am.tweetmeck.ui.home.TimelinePoolProvider
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import timber.log.Timber

abstract class TweetListFragment : Fragment() {

    private lateinit var adapter: TweetListAdapter
    private lateinit var binding: FragmentTweetListBinding
    private val viewPoolProvider: TimelinePoolProvider? by lazy {
        parentFragment?.currentScope?.get<TimelinePoolProvider>()
    }

    @Suppress("UNCHECKED_CAST")
    abstract var viewModel: TweetListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel(from = { parentFragment as ViewModelStoreOwner })

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

        binding.recyclerview.apply {
            adapter = this@TweetListFragment.adapter
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

        viewModel.timelineContent.observe(viewLifecycleOwner, Observer { timelineContent ->

            timelineContent.pagedList.observe(viewLifecycleOwner, Observer<PagedList<Tweet>> {
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

            timelineContent.loadMoreState.observe(this, Observer {
                Timber.d("Request state! $it")
            })
        })
    }
}
