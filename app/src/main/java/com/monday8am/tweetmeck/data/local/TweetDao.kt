package com.monday8am.tweetmeck.data.local

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.monday8am.tweetmeck.data.models.Tweet

@Dao
interface TweetDao {

    @Query("SELECT * FROM tweets WHERE id = :id")
    suspend fun getItemById(id: Long): Tweet?

    @Query("SELECT * FROM tweets WHERE list_id = :listId ORDER BY id DESC")
    fun getTweetsByListId(listId: Long): DataSource.Factory<Int, Tweet>

    @Query("DELETE FROM tweets WHERE retweeted_id = :id AND main_user_id = :userId")
    suspend fun deleteTweetWithRetweetedId(id: Long, userId: Long)

    @Transaction
    suspend fun refreshTweetsFromList(listId: Long, tweets: List<Tweet>) {
        deleteByList(listId)
        insert(tweets)
    }

    @Transaction
    suspend fun deleteAndUpdateTweets(tweetedId: Long, toUpdate: List<Tweet>) {
        delete(tweetedId)
        insert(toUpdate)
    }

    @Query("SELECT * FROM tweets WHERE retweeted_id = :retweetedId OR main_id = :retweetedId")
    suspend fun getRelatedTweets(retweetedId: Long): List<Tweet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Tweet)

    @Query("DELETE FROM tweets WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<Tweet>)

    @Query("DELETE FROM tweets WHERE list_id = :listId")
    suspend fun deleteByList(listId: Long)

    @Query("DELETE FROM tweets")
    suspend fun clear()
}
