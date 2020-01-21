package com.monday8am.tweetmeck.ui.search

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.monday8am.tweetmeck.databinding.FragmentSearchBinding
import com.monday8am.tweetmeck.util.EventObserver
import com.monday8am.tweetmeck.util.TimelinePoolProvider
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope

class SearchFragment : Fragment() {

    private val navArgs: SearchFragmentArgs by navArgs()
    private val searchViewModel: SearchViewModel by inject()
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@SearchFragment.searchViewModel
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    dismissKeyboard(this@apply)
                    searchViewModel.searchFor(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return true
                }
            })
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Show an error message
        searchViewModel.timelineErrorMessage.observe(viewLifecycleOwner, EventObserver { errorMsg ->
            // TODO: Change once there's a way to show errors to the user
            Toast.makeText(this.context, errorMsg, Toast.LENGTH_LONG).show()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchViewModel.searchQuery.observe(viewLifecycleOwner, EventObserver { query ->
            binding.toolbar.searchView.setQuery(query.hashtag, false)
        })

        searchViewModel.timelineContent.observe(viewLifecycleOwner, Observer {
            val (_, timelineContent) = it
            binding.searchTimelineView.bind(
                timelineContent,
                searchViewModel,
                viewLifecycleOwner
            )
        })

        searchViewModel.openUrl.observe(viewLifecycleOwner, EventObserver {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
        })

        searchViewModel.navigateToTweetDetails.observe(viewLifecycleOwner, EventObserver { tweetId ->
            findNavController().navigate(SearchFragmentDirections.actionSearchToTweet(tweetId))
        })

        searchViewModel.navigateToUserDetails.observe(viewLifecycleOwner, EventObserver { screenName ->
            findNavController().navigate(SearchFragmentDirections.actionSearchToUser(screenName))
        })

        searchViewModel.navigateToSearch.observe(viewLifecycleOwner, EventObserver { searchItem ->
            searchViewModel.searchFor(searchItem)
        })

        searchViewModel.searchFor(navArgs.searchItem)
    }

    override fun onPause() {
        dismissKeyboard(binding.toolbar.searchView)
        super.onPause()
    }

    private fun showKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }

    private fun dismissKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
