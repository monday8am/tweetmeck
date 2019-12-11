package com.monday8am.tweetmeck.data.models.entities

data class MediaEntity(
    val start: Int,
    val end: Int,
    val displayUrl: String,
    val url: String,
    val expandedUrl: String,
    val type: String,
    val mediaUrlHttps: String
)
