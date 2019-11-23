package com.monday8am.tweetmeck.data.local

import androidx.paging.DataSource
import androidx.room.*
import com.monday8am.tweetmeck.data.models.Tweet

@Dao
interface TweetDao {

    @Query("SELECT * FROM tweets")
    suspend fun getAll(): List<Tweet>

    @Query("SELECT * FROM tweets WHERE id = :id")
    suspend fun getItemById(id: Long): Tweet?

    @Query("SELECT * FROM tweets WHERE list_id = :listId")
    suspend fun getTweetsByListId(listId: Long): DataSource.Factory<Long, Tweet>

    @Transaction
    suspend fun updateAll(items: List<Tweet>) {
        deleteAll()
        insertAll(items)
    }

    @Transaction
    suspend fun insertTweetsFromList(listId: Long, List<Tweet>) {

    }

    @Insert
    suspend fun insertAll(items: List<Tweet>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Tweet): Long

    @Query("DELETE FROM tweets")
    suspend fun deleteAll()
}
