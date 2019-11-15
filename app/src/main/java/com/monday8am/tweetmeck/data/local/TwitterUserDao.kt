package com.monday8am.tweetmeck.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.monday8am.tweetmeck.data.models.TwitterUser

@Dao
interface TwitterUserDao {

    @Query("SELECT * FROM twitterUsers")
    suspend fun getAll(): List<TwitterUser>

    @Query("SELECT * FROM twitterUsers WHERE id = :id")
    suspend fun getItemById(id: Long): TwitterUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TwitterUser): Long

    @Query("DELETE FROM twitterUsers")
    suspend fun deleteAll()

}
