package com.monday8am.tweetmeck.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.databinding.TimelineFragmentBinding
import com.monday8am.tweetmeck.util.getViewModelFactory
import timber.log.Timber

class TimelineFragment : Fragment() {

    private lateinit var binding: TimelineFragmentBinding

    private val viewModel by viewModels<TimelineViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TimelineFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.twitterLists.observe(viewLifecycleOwner, Observer<List<TwitterList>> { lists ->
            lists.forEach { Timber.d(it.toString()) }
        })

        viewModel.dataLoading.observe(viewLifecycleOwner, Observer<Boolean> {
            Timber.d("Loading: $it")
        })
    }
}
