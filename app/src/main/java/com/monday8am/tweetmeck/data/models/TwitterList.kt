package com.monday8am.tweetmeck.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ListVisibilityMode {
    Default,
    Public,
    Private
}

@Entity(tableName = "lists")
data class TwitterList(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val description: String,
    val following: Boolean,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "member_count") val memberCount: Int,
    @ColumnInfo(name = "visibility_mode") val mode: ListVisibilityMode,
    val name: String,
    val slug: String,
    @ColumnInfo(name = "subscriber_count") val subscriberCount: Int,
    val uri: String,
    @ColumnInfo(name = "user_id") val userId: Long
)
