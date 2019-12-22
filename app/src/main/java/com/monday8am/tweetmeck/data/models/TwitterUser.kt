package com.monday8am.tweetmeck.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class TwitterUser(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val name: String,
    @ColumnInfo(name = "screen_name") val screenName: String,
    val location: String,
    val description: String,
    val url: String?,
    val verified: Boolean,

    @ColumnInfo(name = "followers_count") val followersCount: Int,
    @ColumnInfo(name = "friends_count") val friendsCount: Int,
    @ColumnInfo(name = "listed_count") val listedCount: Int,
    @ColumnInfo(name = "favourites_count") val favouritesCount: Int,
    @ColumnInfo(name = "statuses_count") val statusesCount: Int,

    @ColumnInfo(name = "profile_background_color") val profileBackgroundColor: String,
    @ColumnInfo(name = "profile_background_image_url") val profileBackgroundImageUrl: String?,
    @ColumnInfo(name = "profile_background_tile") val profileBackgroundTile: Boolean,
    @ColumnInfo(name = "profile_image_url") val profileImageUrl: String,
    @ColumnInfo(name = "default_profile_image") val defaultProfileImage: Boolean,

    val following: Boolean,
    @ColumnInfo(name = "follow_request_sent") val followRequestSent: Boolean,
    @ColumnInfo(name = "logged_user") val loggedUser: Boolean = false
)
