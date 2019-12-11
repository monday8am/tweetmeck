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
    @ColumnInfo(name = "verified") val verified: Boolean
)

@Entity(tableName = "tweets")
data class Tweet(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "id_str") val idStr: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "full_content") val fullContent: String?,
    val truncated: Boolean,
    val source: String,

    @ColumnInfo(name = "entities") val entities: List<UrlEntity>,

    @ColumnInfo(name = "in_reply_to_screen_name") val inReplyToScreenName: String?,
    @ColumnInfo(name = "in_reply_to_status_id") val inReplyToStatusId: Long?,
    @ColumnInfo(name = "in_reply_to_status_id_str") val inReplyToStatusIdStr: String?,
    @ColumnInfo(name = "in_reply_to_user_id") val inReplyToUserId: Long?,
    @ColumnInfo(name = "in_reply_to_user_id_str") val inReplyToUserIdStr: String?,

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
        fun from(dto: Status, listId: Long): Tweet {
            val unescapedContent = TweetUtils.unescapeTweetContent(dto.fullTextRaw ?: "")
            val subrogatedIndexes = TweetUtils.getHighSurrogateIndices(unescapedContent.first)
            val entities = (
                    dto.entities.hashtags.map { UrlEntity.from(it) } +
                    dto.entities.urls.map { UrlEntity.from(it) } +
                    dto.entities.userMentions.map { UrlEntity.from(it) } +
                    dto.entities.symbols.map { UrlEntity.from(it) })
                .sortByStartIndex()
                .adjustIndicesForEscapedChars(unescapedContent.second)
                .adjustEntitiesWithOffsets(subrogatedIndexes)

            dto.retweetedStatus

            return Tweet(
                dto.id,
                dto.idStr,
                TweetDateUtils.apiTimeToLong(dto.createdAtRaw),
                dto.textRaw,
                unescapedContent.first,
                dto.truncated,
                dto.source,
                entities,
                dto.inReplyToScreenName,
                dto.inReplyToStatusId,
                dto.inReplyToStatusIdStr,
                dto.inReplyToUserId,
                dto.inReplyToUserIdStr,
                listId,
                TimelineUser(
                    dto.user.id,
                    dto.user.name,
                    dto.user.screenName,
                    dto.user.profileImageUrl,
                    dto.user.verified
                ),
                dto.quotedStatusId,
                dto.isQuoteStatus,
                dto.retweetCount,
                dto.favoriteCount,
                dto.favorited,
                dto.retweeted,
                false,
                dto.langRaw)
        }
    }
}
