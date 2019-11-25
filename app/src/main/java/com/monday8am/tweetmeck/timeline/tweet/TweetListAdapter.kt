package com.monday8am.tweetmeck.timeline.tweet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.data.Loaded
import com.monday8am.tweetmeck.data.RequestState
import com.monday8am.tweetmeck.data.Result.Loading
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.databinding.ItemRequestStateBinding
import com.monday8am.tweetmeck.databinding.ItemTweetBinding
import com.monday8am.tweetmeck.timeline.TweetItemEventListener
import timber.log.Timber

class TweetListAdapter(
    private val listId: Long,
    private val eventListener: TweetItemEventListener,
    private val lifecycleOwner: LifecycleOwner
) : PagedListAdapter<Tweet, RecyclerView.ViewHolder>(TweetItemDiff) {

    private var requestState: RequestState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val binding = ItemTweetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TweetViewHolder(
            binding, eventListener, lifecycleOwner
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_tweet -> {
                val item = getItem(position) ?: return
                (holder as TweetViewHolder).bind(item)
            }
            R.layout.item_request_state -> (holder as NetworkStateViewHolder).bind(listId, requestState)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            Timber.d("onBindViewHolder payload update!")
        } else {
            onBindViewHolder(holder, position)
        }
    }

    private fun hasExtraRow() = requestState?.Loaded ?: false

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.item_request_state
        } else {
            R.layout.item_tweet
        }
    }

    fun setRequestState(requestState: RequestState?) {
        val previousState = this.requestState
        val hadExtraRow = hasExtraRow()
        this.requestState = requestState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != requestState) {
            notifyItemChanged(itemCount - 1)
        }
    }
}

class TweetViewHolder(
    private val binding: ItemTweetBinding,
    private val eventListener: TweetItemEventListener,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(tweet: Tweet) {
        binding.tweet = tweet
        binding.eventListener = eventListener
        binding.lifecycleOwner = lifecycleOwner
        binding.executePendingBindings()
    }
}

class NetworkStateViewHolder(
    private val binding: ItemRequestStateBinding,
    private val eventListener: TweetItemEventListener,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(listId: Long, state: RequestState?) {
        binding.listId = listId
        binding.state = state ?: Loading
        binding.eventListener = eventListener
        binding.lifecycleOwner = lifecycleOwner
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
        return oldItem == newItem
    }
}
