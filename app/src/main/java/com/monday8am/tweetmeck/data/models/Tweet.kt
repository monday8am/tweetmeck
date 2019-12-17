package com.monday8am.tweetmeck.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monday8am.tweetmeck.data.models.entities.MediaEntity
import com.monday8am.tweetmeck.data.models.entities.UrlEntity
import com.monday8am.tweetmeck.util.TweetDateUtils
import jp.nephy.penicillin.models.Status

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

    fun setRetweetCount(newValue: Int): Tweet {
        return if (retweetedContent != null) {
            this.copy(retweetedContent = retweetedContent.copy(retweetCount = newValue))
        } else {
            this.copy(content = content.copy(retweetCount = newValue))
        }
    }

    private fun setFavorite(content: TweetContent, newValue: Boolean): TweetContent {
        return content.copy(
            favorited = newValue,
            favoriteCount = if (newValue) content.favoriteCount + 1 else content.favoriteCount - 1)
    }

    companion object {
        fun from(status: Status, listId: Long): Tweet {
            return Tweet(
                id = status.id,
                content = getTweetContent(status)!!,
                retweetedContent = getTweetContent(status.retweetedStatus),
                quotedContent = getTweetContent(status.quotedStatus ?: status.retweetedStatus?.quotedStatus),
                truncated = status.truncated,
                source = status.source,
                listId = listId,
                inReplyToScreenName = status.inReplyToScreenName,
                inReplyToStatusId = status.inReplyToStatusId,
                inReplyToUserId = status.inReplyToUserId)
        }

        private fun getTimelineUser(status: Status): TimelineUser {
            return TimelineUser(
                status.user.id,
                status.user.name,
                status.user.screenName,
                status.user.profileImageUrl,
                status.user.verified
            )
        }

        private fun getTweetContent(dtoStatus: Status?): TweetContent? {
            val status = dtoStatus ?: return null
            val unescapedContent = getUnescapedContent(status)

            return TweetContent(
                id = status.id,
                createdAt = TweetDateUtils.apiTimeToLong(dtoStatus.createdAtRaw),
                fullText = getTextWithoutUrls(unescapedContent.first, status),
                user = getTimelineUser(status),
                urlEntities = getUrlEntities(status, unescapedContent),
                mediaEntities = getMediaEntities(status),
                retweetCount = status.retweetCount,
                favoriteCount = status.favoriteCount,
                favorited = status.favorited,
                retweeted = status.retweeted)
        }

        private fun getUnescapedContent(tweet: Status): Pair<String, List<IntArray>> {
            return TweetUtils.unescapeTweetContent(tweet.fullTextRaw ?: "")
        }

        private fun getUrlEntities(
            status: Status,
            unescapedTweetContent: Pair<String, List<IntArray>>
        ): List<UrlEntity> {
            val subrogatedIndexes = TweetUtils.getHighSurrogateIndices(unescapedTweetContent.first)
            var urls = status.entities.urls.map { UrlEntity.from(it) }

            if (status.quotedStatus != null) {
                urls = urls.take(urls.size - 1)
            }

            return (status.entities.hashtags.map { UrlEntity.from(it) } +
                    urls +
                    status.entities.userMentions.map { UrlEntity.from(it) } +
                    status.entities.symbols.map { UrlEntity.from(it) })
                    .sortByStartIndex()
                    .adjustIndicesForEscapedChars(unescapedTweetContent.second)
                    .adjustEntitiesWithOffsets(subrogatedIndexes)
        }

        private fun getMediaEntities(tweet: Status): List<MediaEntity> {
            return tweet.extendedEntities?.media?.map { MediaEntity.from(it) } ?: listOf()
        }

        private fun getTextWithoutUrls(
            content: String,
            status: Status
        ): String {
            var result = content
            if (status.entities.media.isNotEmpty()) {
                result = result.replace(status.entities.media.first().url, "", ignoreCase = true)
            }

            if (status.quotedStatus != null) {
                result = result.replace(status.entities.urls.last().url, "", ignoreCase = true)
            }

            return result.trimEnd()
        }
    }
}
