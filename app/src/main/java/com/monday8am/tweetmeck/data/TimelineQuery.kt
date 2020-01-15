package com.monday8am.tweetmeck.data

sealed class TimelineQuery {
    data class List(val listId: Long) : TimelineQuery()
    data class User(val userId: Long) : TimelineQuery()
    data class Hashtag(val hashtag: String) : TimelineQuery()

    companion object {
        fun fromFormattedString(url: String): TimelineQuery {
            val parsed = url.split(":")
            return when (parsed[0]) {
                "list" -> List(parsed[1].toLong())
                "user" -> User(parsed[1].toLong())
                else -> Hashtag(parsed[1])
            }
        }
    }

    fun toFormattedString(): String {
        return when (this) {
            is List -> "list:${this.listId}"
            is User -> "user:${this.userId}"
            is Hashtag -> "hashtag:${this.hashtag}"
        }
    }
}
