package com.monday8am.tweetmeck.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monday8am.tweetmeck.data.models.TwitterList

@Database(entities = [TwitterList::class], version = 1, exportSchema = false)
@TypeConverters(RoomConverters::class)
abstract class TwitterDatabase: RoomDatabase() {
    abstract fun twitterListDao(): TwitterListDao
}
