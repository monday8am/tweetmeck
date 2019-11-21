package com.monday8am.tweetmeck.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.models.TwitterUser

@Database(entities = [TwitterList::class, TwitterUser::class, Tweet::class], version = 1, exportSchema = false)
@TypeConverters(RoomConverters::class)
abstract class TwitterDatabase : RoomDatabase() {
    abstract fun twitterListDao(): TwitterListDao
    abstract fun twitterUserDao(): TwitterUserDao
    abstract fun tweetDao(): TweetDao
}
