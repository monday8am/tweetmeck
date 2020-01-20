package com.monday8am.tweetmeck.ui.timeline

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.util.TimelinePoolProvider

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyle, defStyleRes) {

    private lateinit var adapter: TimelineAdapter
    private var recyclerView: RecyclerView

    init {
        LayoutInflater.from(context).inflate(R.layout.timeline_view, this)
        recyclerView = findViewById(R.id.timeline_recycler)
    }

    fun bind(
        viewModel: TimelineViewModel,
        viewLifecycleOwner: LifecycleOwner,
        viewPoolProvider: TimelinePoolProvider?
    ) {

        val textCreator = TweetItemTextCreator(this.context, viewModel.currentSession)
        adapter = TimelineAdapter(viewModel, viewLifecycleOwner, textCreator)

        recyclerView.let {
            it.addItemDecoration(DividerItemDecoration(
                it.context,
                DividerItemDecoration.VERTICAL
            ))

            it.apply {
                adapter = this@TimelineView.adapter
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
        }

        viewModel.timelineContent.value?.pagedList?.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it) {
                // Workaround for an issue where RecyclerView incorrectly uses the loading / spinner
                // item added to the end of the list as an anchor during initial load.
                val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)
                val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (position != RecyclerView.NO_POSITION) {
                    recyclerView.scrollToPosition(position)
                }
            }
        })
    }

    fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }
}
