package com.monday8am.tweetmeck.ui.home.timeline

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
import com.monday8am.tweetmeck.ui.home.HomeViewModel
import com.monday8am.tweetmeck.ui.home.TimelinePoolProvider
import com.monday8am.tweetmeck.util.lazyFast
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import timber.log.Timber

class TimelineFragment : Fragment() {

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

    private lateinit var adapter: TimelineAdapter
    private lateinit var binding: FragmentTweetListBinding
    private val viewPoolProvider: TimelinePoolProvider? by lazy {
        parentFragment?.currentScope?.get<TimelinePoolProvider>()
    }

    @Suppress("UNCHECKED_CAST")
    private lateinit var viewModel: HomeViewModel

    private val listId: Long by lazyFast {
        val args = arguments ?: throw IllegalStateException("Missing arguments!")
        args.getLong(ARG_LIST_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel(from = { parentFragment as ViewModelStoreOwner })

        binding = FragmentTweetListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@TimelineFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TimelineAdapter(viewModel, viewLifecycleOwner, TimelineTextCreator(view.context))

        binding.recyclerview.addItemDecoration(DividerItemDecoration(
            binding.recyclerview.context,
            DividerItemDecoration.VERTICAL
        ))

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

        val content = viewModel.getTimelineContent(listId)
        content.pagedList.observe(this, Observer<PagedList<Tweet>> {
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

        content.loadMoreState.observe(this, Observer {
            Timber.d("Request state! $it")
        })
    }
}
