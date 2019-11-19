package com.monday8am.tweetmeck.timeline.tweet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.monday8am.tweetmeck.databinding.FragmentTweetListBinding
import com.monday8am.tweetmeck.timeline.TimelineViewModel
import com.monday8am.tweetmeck.util.lazyFast

class TweetListFragment : Fragment() {

    companion object {

        private const val TAG = "TweetListFragment"
        private const val ARG_LIST_ID = "arg.TWEET_LIST_ID"

        @JvmStatic
        fun newInstance(listId: Long) =
            TweetListFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_LIST_ID, listId)
                }
            }
    }

    private lateinit var adapter: TweetListAdapter
    private lateinit var viewModel: TimelineViewModel
    private lateinit var binding: FragmentTweetListBinding

    private val listId: Long by lazyFast {
        val args = arguments ?: throw IllegalStateException("Missing arguments!")
        args.getLong(ARG_LIST_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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

        adapter = TweetListAdapter(viewModel, viewLifecycleOwner)
    }
}
