package com.monday8am.tweetmeck.data.mappers

import com.monday8am.tweetmeck.data.models.*
import com.monday8am.tweetmeck.data.models.entities.MediaEntity
import com.monday8am.tweetmeck.data.models.entities.UrlEntity
import com.monday8am.tweetmeck.util.TweetDateUtils
import jp.nephy.penicillin.extensions.models.text
import jp.nephy.penicillin.models.Status

class StatusToTweet(private val listId: Long) : Mapper<Status, Tweet> {
    override fun map(from: Status): Tweet {
        return Tweet(
            id = from.id,
            main = getTweetContent(from)!!,
            retweet = getTweetContent(from.retweetedStatus),
            quote = getTweetContent(from.quotedStatus ?: from.retweetedStatus?.quotedStatus),
            truncated = from.truncated,
            source = from.source,
            listId = listId,
            inReplyToScreenName = from.inReplyToScreenName,
            inReplyToStatusId = from.inReplyToStatusId,
            inReplyToUserId = from.inReplyToUserId)
    }

    private fun getTimelineUser(status: Status): UiUser {
        return UiUser(
            status.user.id,
            status.user.name,
            status.user.screenName,
            status.user.profileImageUrl,
            status.user.verified
        )
    }

    private fun getTweetContent(dtoStatus: Status?): UiTweet? {
        val status = dtoStatus ?: return null
        val unescapedContent = getUnescapedContent(status)

        return UiTweet(
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
        return TweetUtils.unescapeTweetContent(tweet.text)
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
