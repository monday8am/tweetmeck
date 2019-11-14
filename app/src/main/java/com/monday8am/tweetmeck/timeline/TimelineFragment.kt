package com.monday8am.tweetmeck.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.monday8am.tweetmeck.databinding.TimelineFragmentBinding
import com.monday8am.tweetmeck.util.getViewModelFactory

class TimelineFragment : Fragment() {

    private lateinit var binding: TimelineFragmentBinding

    private val viewModel by viewModels<TimelineViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TimelineFragmentBinding.inflate(layoutInflater)
        binding.triggerBtn.setOnClickListener {
            viewModel.getList()
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}
