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

@Entity(tableName = "tweets")
data class Tweet(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "full_content") val fullContent: String?,
    val truncated: Boolean,
    val source: String,

    @ColumnInfo(name = "list_id") val listId: Long,
    @Embedded val timelineUser: TimelineUser,
    @ColumnInfo(name = "url_entities") val urlEntities: List<UrlEntity>,
    @ColumnInfo(name = "media_entities") val mediaEntities: List<MediaEntity>,

    // reply tweet!
    @ColumnInfo(name = "in_reply_to_screen_name") val inReplyToScreenName: String?,
    @ColumnInfo(name = "in_reply_to_status_id") val inReplyToStatusId: Long?,
    @ColumnInfo(name = "in_reply_to_user_id") val inReplyToUserId: Long?,

    // retweeted tweet
    @ColumnInfo(name = "retweeted_by_screen_name") val retweetedByScreenName: String?,
    @ColumnInfo(name = "retweeted_status_id") val retweetedStatusId: Long?,
    @ColumnInfo(name = "retweeted_by_user_id") val retweetedByUserId: Long?,

    // quoted tweet!
    @ColumnInfo(name = "quoted_status_id") val quotedStatusId: Long?,
    @ColumnInfo(name = "is_quote_status") val isQuoteStatus: Boolean,

    @ColumnInfo(name = "retweet_count") val retweetCount: Int,
    @ColumnInfo(name = "favorite_count") val favoriteCount: Int,

    val favorited: Boolean,
    val retweeted: Boolean,

    @ColumnInfo(name = "possibly_sensitive") val possiblySensitive: Boolean,
    @ColumnInfo(name = "lang_raw") val langRaw: String

) {
    companion object {
        fun from(dtoStatus: Status, listId: Long): Tweet {
            val isRetweet = dtoStatus.retweetedStatus != null
            val status = dtoStatus.retweetedStatus ?: dtoStatus
            val unescapedContent = getUnescapedContent(status)
            val indices = TweetUtils.getHighSurrogateIndices(unescapedContent.first)

            return Tweet(
                id = dtoStatus.id,
                createdAt = TweetDateUtils.apiTimeToLong(dtoStatus.createdAtRaw),
                content = status.textRaw,
                fullContent = unescapedContent.first,
                truncated = status.truncated,
                source = status.source,
                listId = listId,
                timelineUser = getTimelineUser(status),
                urlEntities = getUrlEntities(status, unescapedContent, indices),
                mediaEntities = getMediaEntities(status),

                inReplyToScreenName = status.inReplyToScreenName,
                inReplyToStatusId = status.inReplyToStatusId,
                inReplyToUserId = status.inReplyToUserId,

                retweetedByScreenName = if (isRetweet) dtoStatus.user.screenName else null,
                retweetedByUserId = if (isRetweet) dtoStatus.user.id else null,
                retweetedStatusId = if (isRetweet) dtoStatus.retweetedStatus?.id else null,

                quotedStatusId = status.quotedStatusId,
                isQuoteStatus = status.isQuoteStatus,
                retweetCount = status.retweetCount,
                favoriteCount = status.favoriteCount,

                favorited = status.favorited,
                retweeted = status.retweeted,

                possiblySensitive = false,
                langRaw = status.langRaw)
        }

        private fun getTimelineUser(tweet: Status): TimelineUser {
            return TimelineUser(
                tweet.user.id,
                tweet.user.name,
                tweet.user.screenName,
                tweet.user.profileImageUrl,
                tweet.user.verified
            )
        }

        private fun getUnescapedContent(tweet: Status): Pair<String, List<IntArray>> {
            return TweetUtils.unescapeTweetContent(tweet.fullTextRaw ?: "")
        }

        private fun getUrlEntities(tweet: Status,
                                   unescapedContent: Pair<String, List<IntArray>>,
                                   subrogatedIndexes: List<Int>): List<UrlEntity> {
            return (tweet.entities.hashtags.map { UrlEntity.from(it) } +
                    tweet.entities.urls.map { UrlEntity.from(it) } +
                    tweet.entities.userMentions.map { UrlEntity.from(it) } +
                    tweet.entities.symbols.map { UrlEntity.from(it) })
                    .sortByStartIndex()
                    .adjustIndicesForEscapedChars(unescapedContent.second)
                    .adjustEntitiesWithOffsets(subrogatedIndexes)
        }

        private fun getMediaEntities(tweet: Status): List<MediaEntity> {
            return tweet.entities.media.map { UrlEntity.from(it) }
                .sortByStartIndex()
                .adjustIndicesForEscapedChars(unescapedContent.second)
                .adjustEntitiesWithOffsets(subrogatedIndexes)
        }


    }
}
