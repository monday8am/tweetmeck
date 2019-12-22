package com.monday8am.tweetmeck.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monday8am.tweetmeck.data.models.entities.MediaEntity
import com.monday8am.tweetmeck.data.models.entities.UrlEntity

data class TimelineUser(
    @ColumnInfo(name = "user_id") val id: Long,
    @ColumnInfo(name = "user_name")val name: String,
    @ColumnInfo(name = "screen_name") val screenName: String,
    @ColumnInfo(name = "profile_image_url") val profileImageUrl: String,
    @ColumnInfo(name = "verified") val verified: Boolean
)

data class TweetContent(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "full_text") val fullText: String,
    @Embedded val user: TimelineUser,
    @ColumnInfo(name = "url_entities") val urlEntities: List<UrlEntity>,
    @ColumnInfo(name = "media_entities") val mediaEntities: List<MediaEntity>,
    @ColumnInfo(name = "retweet_count") val retweetCount: Int,
    @ColumnInfo(name = "favorite_count") val favoriteCount: Int,
    val favorited: Boolean,
    val retweeted: Boolean
)

@Entity(tableName = "tweets")
data class Tweet(
    @PrimaryKey val id: Long,
    @Embedded(prefix = "main_") val content: TweetContent,
    @Embedded(prefix = "retweeted_") val retweetedContent: TweetContent?,
    @Embedded(prefix = "quoted_") val quotedContent: TweetContent?,

    val truncated: Boolean,
    val source: String,
    @ColumnInfo(name = "list_id") val listId: Long,
    @ColumnInfo(name = "in_reply_to_screen_name") val inReplyToScreenName: String?,
    @ColumnInfo(name = "in_reply_to_status_id") val inReplyToStatusId: Long?,
    @ColumnInfo(name = "in_reply_to_user_id") val inReplyToUserId: Long?
) {

    val tweetContent: TweetContent
        get() {
            return retweetedContent ?: content
        }

    val isCached: Boolean
        get() {
            return listId != -1L
        }

    val hasQuote: Boolean
        get() {
            return quotedContent != null
        }

    val hasRetweeted: Boolean
        get() {
            return retweetedContent != null
        }

    fun setFavorite(newValue: Boolean): Tweet {
        return if (retweetedContent != null) {
            this.copy(retweetedContent = setFavorite(retweetedContent, newValue))
        } else {
            this.copy(content = setFavorite(content, newValue))
        }
    }

    fun setRetweeted(newValue: Boolean): Tweet {
        return if (retweetedContent != null) {
            this.copy(retweetedContent = setRetweeted(retweetedContent, newValue))
        } else {
            this.copy(content = setRetweeted(content, newValue))
        }
    }

    private fun setFavorite(content: TweetContent, newValue: Boolean): TweetContent {
        return content.copy(
            favorited = newValue,
            favoriteCount = if (newValue) content.favoriteCount + 1 else content.favoriteCount - 1)
    }

    private fun setRetweeted(content: TweetContent, newValue: Boolean): TweetContent {
        return content.copy(
            retweeted = newValue,
            retweetCount = if (newValue) content.retweetCount + 1 else content.retweetCount - 1)
    }
}
