package com.monday8am.tweetmeck.util

import androidx.recyclerview.widget.RecyclerView

class TimelinePoolProvider {
    private var _tweetItemPool: RecyclerView.RecycledViewPool? = null
    val tweetItemPool: RecyclerView.RecycledViewPool
        get() {
            val pool = _tweetItemPool ?: RecyclerView.RecycledViewPool()
            _tweetItemPool = pool
            return pool
        }
}
