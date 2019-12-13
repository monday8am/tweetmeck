package com.monday8am.tweetmeck.data.models.entities

data class MediaEntity(
    val start: Int,
    val end: Int,
    val displayUrl: String,
    val url: String,
    val expandedUrl: String,
    val mediaUrlHttps: String,
    val type: String
) {
    fun toEntityString(): String {
        return "${start}\n${end}\n${url}\n${displayUrl}\n${expandedUrl}\n$mediaUrlHttps\n$type"
    }

    companion object {
        fun from(media: jp.nephy.penicillin.models.entities.MediaEntity): MediaEntity {
            return MediaEntity(
                start = media.indices.first(),
                end = media.indices.last(),
                displayUrl = media.displayUrl,
                url = media.url,
                expandedUrl = media.expandedUrl,
                mediaUrlHttps = media.mediaUrlHttps,
                type = media.type
            )
        }

        fun fromEntityString(content: String): MediaEntity {
            val values = content.split("\n")
            return MediaEntity(
                start = values[0].toInt(),
                end = values[1].toInt(),
                url = values[2],
                displayUrl = values[3],
                expandedUrl = values[4],
                mediaUrlHttps = values[5],
                type = values[6]
            )
        }
    }
}
