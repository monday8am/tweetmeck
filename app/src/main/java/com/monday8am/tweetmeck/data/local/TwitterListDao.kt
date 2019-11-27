package com.monday8am.tweetmeck.data.local

import androidx.room.*
import com.monday8am.tweetmeck.data.models.TwitterList

@Dao
interface TwitterListDao {

    @Query("SELECT * FROM lists")
    suspend fun getAll(): List<TwitterList>

    @Query("SELECT * FROM lists WHERE id = :id")
    suspend fun getItemById(id: Long): TwitterList?

    @Transaction
    suspend fun updateAll(items: List<TwitterList>) {
        deleteAll()
        insertAll(items)
    }

    @Insert
    suspend fun insertAll(items: List<TwitterList>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TwitterList): Long

    @Query("DELETE FROM lists")
    suspend fun deleteAll()
}
