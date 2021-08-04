package com.monday8am.tweetmeck.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.monday8am.tweetmeck.data.models.TwitterList
import kotlinx.coroutines.flow.Flow

@Dao
interface TwitterListDao {

    @Query("SELECT * FROM lists ORDER BY created_at DESC")
    fun getAll(): Flow<List<TwitterList>>

    @Query("SELECT * FROM lists WHERE id = :id")
    suspend fun getItemById(id: Long): TwitterList?

    @Transaction
    suspend fun updateAll(items: List<TwitterList>) {
        clear()
        insertAll(items)
    }

    @Insert
    suspend fun insertAll(items: List<TwitterList>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TwitterList): Long

    @Query("DELETE FROM lists")
    suspend fun clear()
}
