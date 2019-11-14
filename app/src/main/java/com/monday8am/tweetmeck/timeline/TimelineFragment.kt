package com.monday8am.tweetmeck.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.data.DataRepository
import com.monday8am.tweetmeck.databinding.TimelineFragmentBinding
import com.monday8am.tweetmeck.util.getViewModelFactory

class TimelineFragment(private val dataRepository: DataRepository) : Fragment() {

    private lateinit var binding: TimelineFragmentBinding

    private val viewModel by viewModels<TimelineViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.timeline_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}
