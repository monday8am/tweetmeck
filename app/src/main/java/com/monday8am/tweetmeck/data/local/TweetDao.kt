package com.monday8am.tweetmeck.data.local

import androidx.paging.DataSource
import androidx.room.*
import com.monday8am.tweetmeck.data.models.Tweet

@Dao
interface TweetDao {

    @Query("SELECT * FROM tweets WHERE id = :id")
    suspend fun getItemById(id: Long): Tweet?

    @Query("SELECT * FROM tweets WHERE list_id = :listId ORDER BY id DESC")
    fun getTweetsByListId(listId: Long): DataSource.Factory<Int, Tweet>

    @Query("SELECT COUNT(*) FROM tweets WHERE list_id = :listId")
    suspend fun countTweetsByListId(listId: Long): Int

    @Transaction
    suspend fun refreshTweetsFromList(listId: Long, tweets: List<Tweet>) {
        deleteByList(listId)
        insert(tweets)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Tweet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<Tweet>)

    @Query("DELETE FROM tweets WHERE list_id = :listId")
    suspend fun deleteByList(listId: Long)

    @Query("DELETE FROM tweets")
    suspend fun deleteAll()
}
