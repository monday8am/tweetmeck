package com.monday8am.tweetmeck.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.monday8am.tweetmeck.data.models.TwitterUser

@Dao
interface TwitterUserDao {

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getItemById(id: Long): TwitterUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TwitterUser): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<TwitterUser>)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
