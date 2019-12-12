package com.monday8am.tweetmeck.home.timeline

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.monday8am.tweetmeck.R
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.entities.EntityLinkType
import com.monday8am.tweetmeck.home.TweetItemEventListener
import com.monday8am.tweetmeck.util.TweetDateUtils

class TimelineTextCreator(private val context: Context) {

    fun getUserRetweetText(tweet: Tweet): CharSequence {
        val screenName = tweet.retweetedByScreenName ?: return ""
        return buildSpannedString {
            bold { append(screenName) }
            append(context.getString(R.string.retweeted))
        }
    }

    fun getUserDateText(tweet: Tweet): CharSequence {
        val date = TweetDateUtils.getRelativeTimeString(context, System.currentTimeMillis(), tweet.createdAt)
        return buildSpannedString {
            bold { append(tweet.timelineUser.screenName) }
            append(" | $date")
        }
    }

    fun getTweetDisplayText(tweet: Tweet, listener: TweetItemEventListener): CharSequence {
        val spannable = SpannableStringBuilder(tweet.fullContent ?: "")

        var offset = 0
        var len: Int
        var start: Int
        var end: Int
        for (url in tweet.entities) {
            start = url.start - offset
            end = url.end - offset
            if (start >= 0 && end <= spannable.length) {
                if (url.displayUrl.isNotEmpty()) {
                    spannable.replace(start, end, url.displayUrl)
                    len = end - (start + url.displayUrl.length)
                    end -= len
                    offset += len

                    val span =
                        when (url.entityType) {
                            EntityLinkType.Hashtag -> getClickableSpan(
                                context.getColor(R.color.cyan),
                                listener::searchForTag,
                                url.url
                            )
                            EntityLinkType.Symbol -> getClickableSpan(
                                context.getColor(R.color.green),
                                listener::searchForSymbol,
                                url.url
                            )
                            EntityLinkType.Url -> getClickableSpan(
                                context.getColor(R.color.blue),
                                listener::openUrl,
                                url.url,
                                true
                            )
                            EntityLinkType.MentionedUser -> getClickableSpan(
                                context.getColor(R.color.cyan),
                                listener::openUserDetails,
                                url.url,
                                true
                            )
                        }
                    spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        return spannable
    }

    private fun getClickableSpan(
        color: Int,
        action: (String) -> Unit,
        url: String,
        isBold: Boolean = false
    ): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) = action.invoke(url)
            override fun updateDrawState(ds: TextPaint) {
                ds.color = color
                ds.isFakeBoldText = isBold
                // ds.isUnderlineText = true
            }
        }
    }
}
