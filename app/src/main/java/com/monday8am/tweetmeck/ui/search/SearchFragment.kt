package com.monday8am.tweetmeck.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.databinding.FragmentSearchBinding
import com.monday8am.tweetmeck.ui.timeline.TimelineFragment
import com.monday8am.tweetmeck.util.EventObserver
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.koin.android.ext.android.inject

class SearchFragment : Fragment() {

    private val navArgs: SearchFragmentArgs by navArgs()
    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentSearchBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = this@SearchFragment.viewModel
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.toolbar.searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    dismissKeyboard(this@apply)
                    viewModel.searchFor(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return true
                }
            })

            // Set focus on the SearchView and open the keyboard
            setOnQueryTextFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    showKeyboard(view.findFocus())
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.searchQuery.observe(viewLifecycleOwner, EventObserver { query ->
            val timeline = TimelineFragment.newInstance(query)
            val ft = requireFragmentManager().beginTransaction()
            ft.replace(R.id.timelineContainer, timeline)
            ft.commit()
        })

        viewModel.searchFor(navArgs.searchItem)
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
