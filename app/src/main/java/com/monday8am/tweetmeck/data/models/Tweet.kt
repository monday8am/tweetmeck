package com.monday8am.tweetmeck.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import jp.nephy.penicillin.models.Status

@Entity(tableName = "tweets")
data class Tweet(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "id_str") val idStr: String,
    @ColumnInfo(name = "created_at") val createdAtRaw: String,
    @ColumnInfo(name = "text") val text: String?,
    @ColumnInfo(name = "full_text") val fullText: String?,
    val truncated: Boolean,
    val source: String,

    @ColumnInfo(name = "in_reply_to_screen_name") val inReplyToScreenName: String?,
    @ColumnInfo(name = "in_reply_to_status_id") val inReplyToStatusId: Long?,
    @ColumnInfo(name = "in_reply_to_status_id_str") val inReplyToStatusIdStr: String?,
    @ColumnInfo(name = "in_reply_to_user_id") val inReplyToUserId: Long?,
    @ColumnInfo(name = "in_reply_to_user_id_str") val inReplyToUserIdStr: String?,

    val user: TwitterUser,
    @ColumnInfo(name = "user_id") val userId: Long,

    @ColumnInfo(name = "quoted_status_id") val quotedStatusId: Long?,
    @ColumnInfo(name = "quoted_status_id_str") val quotedStatusIdStr: String?,
    @Ignore val quotedStatus: Tweet?,
    @ColumnInfo(name = "is_quote_status") val isQuoteStatus: Boolean,

    @ColumnInfo(name = "retweet_count") val retweetCount: Int,
    @ColumnInfo(name = "favorite_count") val favoriteCount: Int,

    val favorited: Boolean,
    val retweeted: Boolean,

    @ColumnInfo(name = "possibly_sensitive") val possiblySensitive: Boolean,
    @ColumnInfo(name = "lang_raw") val langRaw: String
) {
    companion object {
        fun from(dto: Status): Tweet {
            return Tweet(
                dto.id,
                dto.idStr,
                dto.createdAtRaw,
                dto.textRaw,
                dto.fullTextRaw,
                dto.truncated,
                dto.source,
                dto.inReplyToScreenName,
                dto.inReplyToStatusId,
                dto.inReplyToStatusIdStr,
                dto.inReplyToUserId,
                dto.inReplyToUserIdStr,
                TwitterUser.from(dto.user),
                dto.user.id,
                dto.quotedStatusId,
                dto.quotedStatusIdStr,
                fromQuotedStatus(dto.quotedStatus),
                dto.isQuoteStatus,
                dto.retweetCount,
                dto.favoriteCount,
                dto.favorited,
                dto.retweeted,
                dto.possiblySensitive,
                dto.langRaw)
        }

        private fun fromQuotedStatus(dtoStatus: Status?): Tweet? {
            dtoStatus ?: return null
            return from(dtoStatus)
        }
    }
}
