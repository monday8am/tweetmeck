package com.monday8am.tweetmeck.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.databinding.TimelineFragmentBinding
import com.monday8am.tweetmeck.util.getViewModelFactory
import timber.log.Timber

class TimelineFragment : Fragment() {

    private lateinit var binding: TimelineFragmentBinding
    private lateinit var viewPager: ViewPager
    private val viewModel by viewModels<TimelineViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TimelineFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@TimelineFragment.viewModel
        }
        viewPager = binding.viewpager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appbar: View = view.findViewById(R.id.appbar)
        val tabs: TabLayout = view.findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                Timber.d("Position changed! $position")
            }
        })
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

    /**
     * Adapter that builds a page for each conference day.

    inner class ScheduleAdapter(fm: FragmentManager, private val labelsForDays: List<Int>) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount() = COUNT
        override fun getItem(position: Int): Fragment = TimelineFragment.newInstance(position)
        override fun getPageTitle(position: Int): CharSequence = getString(labelsForDays[position])
    }
     */
}
