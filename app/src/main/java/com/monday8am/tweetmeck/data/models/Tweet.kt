package com.monday8am.tweetmeck.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monday8am.tweetmeck.data.models.entities.UrlEntity
import com.monday8am.tweetmeck.util.TweetDateUtils
import jp.nephy.penicillin.models.Status

data class TimelineUser(
    @ColumnInfo(name = "user_id") val id: Long,
    @ColumnInfo(name = "user_name")val name: String,
    @ColumnInfo(name = "screen_name") val screenName: String,
    @ColumnInfo(name = "profile_image_url") val profileImageUrl: String,
    @ColumnInfo(name = "verified") val verified: Boolean,
    @ColumnInfo(name = "retweeted_by_id") val retweetedById: Long? = null,
    @ColumnInfo(name = "retweeted_by_screen_name") val retweetedByScreenName: String? = null
)

@Entity(tableName = "tweets")
data class Tweet(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "full_content") val fullContent: String?,
    val truncated: Boolean,
    val source: String,

    @ColumnInfo(name = "entities") val entities: List<UrlEntity>,

    @ColumnInfo(name = "in_reply_to_screen_name") val inReplyToScreenName: String?,
    @ColumnInfo(name = "in_reply_to_status_id") val inReplyToStatusId: Long?,
    @ColumnInfo(name = "in_reply_to_user_id") val inReplyToUserId: Long?,

    @ColumnInfo(name = "list_id") val listId: Long,
    @Embedded val timelineUser: TimelineUser,

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
            val status = dtoStatus.retweetedStatus ?: dtoStatus
            val unescapedContent = getUnescapedContent(status)

            return Tweet(
                dtoStatus.id,
                TweetDateUtils.apiTimeToLong(dtoStatus.createdAtRaw),
                status.textRaw,
                unescapedContent.first,
                status.truncated,
                status.source,
                getEntities(status, unescapedContent),
                status.inReplyToScreenName,
                status.inReplyToStatusId,
                status.inReplyToUserId,
                listId,
                getTimelineUser(status, dtoStatus),
                status.quotedStatusId,
                status.isQuoteStatus,
                status.retweetCount,
                status.favoriteCount,
                status.favorited,
                status.retweeted,
                false,
                status.langRaw)
        }

        private fun getTimelineUser(tweet: Status, originalTweet: Status): TimelineUser {
            var user = TimelineUser(
                tweet.user.id,
                tweet.user.name,
                tweet.user.screenName,
                tweet.user.profileImageUrl,
                tweet.user.verified
            )

            if (tweet.id != originalTweet.id) {
                user = user.copy(retweetedById = originalTweet.user.id,
                                 retweetedByScreenName = originalTweet.user.screenName)
            }

            return user
        }

        private fun getUnescapedContent(tweet: Status): Pair<String, List<IntArray>> {
            return TweetUtils.unescapeTweetContent(tweet.fullTextRaw ?: "")
        }

        private fun getEntities(tweet: Status, unescapedContent: Pair<String, List<IntArray>>): List<UrlEntity> {
            val subrogatedIndexes = TweetUtils.getHighSurrogateIndices(unescapedContent.first)
            return (tweet.entities.hashtags.map { UrlEntity.from(it) } +
                    tweet.entities.urls.map { UrlEntity.from(it) } +
                    tweet.entities.userMentions.map { UrlEntity.from(it) } +
                    tweet.entities.symbols.map { UrlEntity.from(it) })
                    .sortByStartIndex()
                    .adjustIndicesForEscapedChars(unescapedContent.second)
                    .adjustEntitiesWithOffsets(subrogatedIndexes)
        }
    }
}
