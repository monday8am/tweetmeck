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

data class CommonTweetContent(
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
    @Embedded val content: CommonTweetContent,

    @ColumnInfo(name = "retweeted_id") val retweetedId: Long?,
    @Embedded(prefix = "retweeted_") val retweetedContent: CommonTweetContent?,

    @ColumnInfo(name = "quoted_id") val quotedId: Long?,
    @Embedded(prefix = "quoted_") val quotedContent: CommonTweetContent?,

    val truncated: Boolean,
    val source: String,
    @ColumnInfo(name = "list_id") val listId: Long,
    @ColumnInfo(name = "in_reply_to_screen_name") val inReplyToScreenName: String?,
    @ColumnInfo(name = "in_reply_to_status_id") val inReplyToStatusId: Long?,
    @ColumnInfo(name = "in_reply_to_user_id") val inReplyToUserId: Long?
    ) {

    val isCached: Boolean
        get() {
            return listId != -1L
        }

    val hasQuote: Boolean
        get() {
            return quotedContent != null
        }

    val user: TimelineUser
        get() {
            return retweetedContent?.user ?: content.user
        }

    val favoriteCount: Int
        get() {
            return retweetedContent?.favoriteCount ?: content.favoriteCount
        }

    val retweetCount: Int
        get() {
            return retweetedContent?.retweetCount ?: content.retweetCount
        }

    companion object {
        fun from(status: Status, listId: Long): Tweet {
            return Tweet(
                id = status.id,
                content = getTweetContent(status)!!,
                retweetedId = status.retweetedStatus?.id,
                retweetedContent = getTweetContent(status.retweetedStatus),
                quotedId = status.quotedStatus?.id,
                quotedContent = getTweetContent(status.quotedStatus),
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

        private fun getTweetContent(dtoStatus: Status?): CommonTweetContent? {
            val status = dtoStatus ?: return null
            val unescapedContent = getUnescapedContent(status)

            return CommonTweetContent(
                createdAt = TweetDateUtils.apiTimeToLong(dtoStatus.createdAtRaw),
                fullText = getTextWithoutMedia(unescapedContent.first, status.entities.media),
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
            tweet: Status,
            unescapedTweetContent: Pair<String, List<IntArray>>
        ): List<UrlEntity> {
            val subrogatedIndexes = TweetUtils.getHighSurrogateIndices(unescapedTweetContent.first)
            return (tweet.entities.hashtags.map { UrlEntity.from(it) } +
                    tweet.entities.urls.map { UrlEntity.from(it) } +
                    tweet.entities.userMentions.map { UrlEntity.from(it) } +
                    tweet.entities.symbols.map { UrlEntity.from(it) })
                    .sortByStartIndex()
                    .adjustIndicesForEscapedChars(unescapedTweetContent.second)
                    .adjustEntitiesWithOffsets(subrogatedIndexes)
        }

        private fun getMediaEntities(tweet: Status): List<MediaEntity> {
            return tweet.extendedEntities?.media?.map { MediaEntity.from(it) } ?: listOf()
        }

        private fun getTextWithoutMedia(
            content: String,
            entities: List<jp.nephy.penicillin.models.entities.MediaEntity>
        ): String {
            return if (entities.isNotEmpty()) {
                content.replace(entities.first().url, "", ignoreCase = true)
            } else {
                content
            }
        }
    }
}
