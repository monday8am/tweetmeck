package com.monday8am.tweetmeck.data.local

import androidx.paging.DataSource
import androidx.room.*
import com.monday8am.tweetmeck.data.models.Tweet
import timber.log.Timber

@Dao
interface TweetDao {

    @Query("SELECT * FROM tweets WHERE id = :id")
    suspend fun getItemById(id: Long): Tweet?

    @Query("SELECT * FROM tweets WHERE list_id = :listId ORDER BY index_in_response ASC")
    fun getTweetsByListId(listId: Long): DataSource.Factory<Int, Tweet>

    @Transaction
    suspend fun insertTweetsFromList(listId: Long, tweets: List<Tweet>) {
        Timber.d("Original size! ${tweets.size}")
        val tmpIndex = getNextIndexInTweetList(listId)
        val startIndex = tmpIndex ?: 0
        Timber.d("Index to insert List! $startIndex but original is $tmpIndex")
        val items = tweets.mapIndexed { index, child ->
            return@mapIndexed child.copy(indexInResponse = startIndex + index,
                                         listId = listId)
        }
        insert(items)
    }

    @Transaction
    suspend fun refreshTweetsFromList(listId: Long, tweets: List<Tweet>) {
        deleteByList(listId)
        insertTweetsFromList(listId, tweets)
    }

    @Query("SELECT MAX(index_in_response) + 1 FROM tweets WHERE list_id = :listId")
    suspend fun getNextIndexInTweetList(listId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<Tweet>)

    @Query("DELETE FROM tweets WHERE list_id = :listId")
    suspend fun deleteByList(listId: Long)

    @Query("DELETE FROM tweets")
    suspend fun deleteAll()
}
