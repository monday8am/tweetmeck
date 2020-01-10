package com.monday8am.tweetmeck.ui.timeline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.databinding.ItemTweetBinding

class TimelineAdapter(
    private val eventListener: TweetItemEventListener,
    private val lifecycleOwner: LifecycleOwner,
    private val textCreator: TweetItemTextCreator
) : PagedListAdapter<Tweet, TweetViewHolder>(TweetItemDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val binding = ItemTweetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TweetViewHolder(
            binding, textCreator, eventListener, lifecycleOwner
        )
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }
}

class TweetViewHolder(
    private val binding: ItemTweetBinding,
    private val textCreator: TweetItemTextCreator,
    private val eventListener: TweetItemEventListener,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(tweet: Tweet) {
        binding.tweet = tweet
        binding.textCreator = textCreator
        binding.eventListener = eventListener
        binding.lifecycleOwner = lifecycleOwner
        binding.root.setOnClickListener {
            eventListener.openTweetDetails(tweet.id)
        }
        binding.executePendingBindings()
    }
}

object TweetItemDiff : DiffUtil.ItemCallback<Tweet>() {

    override fun areItemsTheSame(
        oldItem: Tweet,
        newItem: Tweet
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Tweet, newItem: Tweet): Boolean {
        return oldItem.uiContent.favorited == newItem.uiContent.favorited &&
                oldItem.uiContent.retweeted == newItem.uiContent.retweeted
    }
}
