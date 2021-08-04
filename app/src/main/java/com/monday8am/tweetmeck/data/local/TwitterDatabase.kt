package com.monday8am.tweetmeck.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monday8am.tweetmeck.data.models.Session
import com.monday8am.tweetmeck.data.models.Tweet
import com.monday8am.tweetmeck.data.models.TwitterList
import com.monday8am.tweetmeck.data.models.TwitterUser

@Database(
    entities = [
        TwitterList::class,
        TwitterUser::class,
        Tweet::class,
        Session::class
    ],
    version = 1, exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class TwitterDatabase : RoomDatabase() {
    abstract fun twitterListDao(): TwitterListDao
    abstract fun twitterUserDao(): TwitterUserDao
    abstract fun tweetDao(): TweetDao
    abstract fun sessionDao(): SessionDao

    companion object {
        fun create(context: Context): TwitterDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                TwitterDatabase::class.java, "Tweetmeck.db"
            ).build()
        }
    }
}
