package com.monday8am.tweetmeck.tweet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.monday8am.tweetmeck.databinding.TweetFragmentBinding
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class TweetFragment : Fragment() {

    private val navArgs: TweetFragmentArgs by navArgs()
    private lateinit var binding: TweetFragmentBinding

    private lateinit var viewModel: TweetViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getViewModel { parametersOf(navArgs.tweetId) }
        binding = TweetFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = this@TweetFragment.viewModel
        }
        return binding.root
    }
}
