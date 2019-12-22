package com.monday8am.tweetmeck.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "screen_name") val screenName: String,
    @ColumnInfo(name = "access_token") val accessToken: String,
    @ColumnInfo(name = "access_secret") val accessTokenSecret: String
)
