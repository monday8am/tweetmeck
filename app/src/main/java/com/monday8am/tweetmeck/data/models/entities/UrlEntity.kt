package com.monday8am.tweetmeck.data.models.entities

import blue.starry.penicillin.models.entities.StatusEntity
import blue.starry.penicillin.models.entities.URLEntity
import com.monday8am.tweetmeck.data.models.TweetUtils

enum class EntityLinkType {
    Hashtag,
    MentionedUser,
    Url,
    Symbol
}

data class UrlEntity(
    val start: Int,
    val end: Int,
    val appUrl: String,
    val displayUrl: String,
    val webUrl: String,
    val entityType: EntityLinkType
) {
    fun toEntityString(): String {
        return "${start}\n${end}\n${appUrl}\n${displayUrl}\n${webUrl}\n$entityType"
    }

    companion object {

        fun from(url: URLEntity): UrlEntity {
            return UrlEntity(
                start = url.indices.first(),
                end = url.indices.last(),
                displayUrl = url.displayUrl,
                appUrl = url.url,
                webUrl = url.expandedUrl,
                entityType = EntityLinkType.Url
            )
        }

        fun from(mentionEntity: StatusEntity.UserMentionEntity): UrlEntity {
            val webUrl = TweetUtils.getProfilePermalink(mentionEntity.screenName)
            return UrlEntity(
                start = mentionEntity.indices.first(),
                end = mentionEntity.indices.last(),
                displayUrl = "@" + mentionEntity.screenName,
                appUrl = mentionEntity.screenName,
                webUrl = webUrl,
                entityType = EntityLinkType.MentionedUser
            )
        }

        fun from(hashtagEntity: StatusEntity.HashtagEntity): UrlEntity {
            val webUrl = TweetUtils.getHashtagPermalink(hashtagEntity.text)
            return UrlEntity(
                start = hashtagEntity.indices.first(),
                end = hashtagEntity.indices.last(),
                appUrl = "#" + hashtagEntity.text,
                displayUrl = "#" + hashtagEntity.text,
                webUrl = webUrl,
                entityType = EntityLinkType.Hashtag
            )
        }

        fun from(symbolEntity: StatusEntity.SymbolEntity): UrlEntity {
            val webUrl = TweetUtils.getSymbolPermalink(symbolEntity.text)
            return UrlEntity(
                start = symbolEntity.indices.first(),
                end = symbolEntity.indices.last(),
                displayUrl = "$" + symbolEntity.text,
                appUrl = "$" + symbolEntity.text,
                webUrl = webUrl,
                entityType = EntityLinkType.Symbol
            )
        }

        fun fromEntityString(content: String): UrlEntity {
            val values = content.split("\n")
            return UrlEntity(
                start = values[0].toInt(),
                end = values[1].toInt(),
                appUrl = values[2],
                displayUrl = values[3],
                webUrl = values[4],
                entityType = EntityLinkType.valueOf(values[5])
            )
        }
    }
}
