package com.monday8am.tweetmeck.data.models.entities

import com.monday8am.tweetmeck.data.models.TweetUtils
import jp.nephy.penicillin.models.entities.StatusEntity
import jp.nephy.penicillin.models.entities.URLEntity

enum class EntityLinkType {
    Hashtag,
    MentionedUser,
    Url,
    Symbol
}

data class UrlEntity(
    val start: Int,
    val end: Int,
    val url: String,
    val displayUrl: String,
    val expandedUrl: String,
    val entityType: EntityLinkType
) {
    fun toEntityString(): String {
        return "${start}\n${end}\n${url}\n${displayUrl}\n${expandedUrl}\n$entityType"
    }

    companion object {

        fun from(url: URLEntity): UrlEntity {
            return UrlEntity(
                start = url.indices.first(),
                end = url.indices.last(),
                displayUrl = url.displayUrl,
                url = url.url,
                expandedUrl = url.expandedUrl,
                entityType = EntityLinkType.Url
            )
        }

        fun from(mentionEntity: StatusEntity.UserMentionEntity): UrlEntity {
            val url = TweetUtils.getProfilePermalink(mentionEntity.screenName)
            return UrlEntity(
                start = mentionEntity.indices.first(),
                end = mentionEntity.indices.last(),
                displayUrl = "@" + mentionEntity.screenName,
                url = url,
                expandedUrl = url,
                entityType = EntityLinkType.MentionedUser
            )
        }

        fun from(hashtagEntity: StatusEntity.HashtagEntity): UrlEntity {
            val url = TweetUtils.getHashtagPermalink(hashtagEntity.text)
            return UrlEntity(
                start = hashtagEntity.indices.first(),
                end = hashtagEntity.indices.last(),
                url = url,
                displayUrl = "#" + hashtagEntity.text,
                expandedUrl = url,
                entityType = EntityLinkType.Hashtag
            )
        }

        fun from(symbolEntity: StatusEntity.SymbolEntity): UrlEntity {
            val url = TweetUtils.getSymbolPermalink(symbolEntity.text)
            return UrlEntity(
                start = symbolEntity.indices.first(),
                end = symbolEntity.indices.last(),
                displayUrl = "$" + symbolEntity.text,
                url = url,
                expandedUrl = url,
                entityType = EntityLinkType.Symbol
            )
        }

        fun fromEntityString(content: String): UrlEntity {
            val values = content.split("\n")
            return UrlEntity(
                start = values[0].toInt(),
                end = values[1].toInt(),
                url = values[2],
                displayUrl = values[3],
                expandedUrl = values[4],
                entityType = EntityLinkType.valueOf(values[5])
            )
        }
    }
}
