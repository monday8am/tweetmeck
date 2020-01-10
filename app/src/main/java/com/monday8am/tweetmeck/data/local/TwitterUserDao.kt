package com.monday8am.tweetmeck.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.monday8am.tweetmeck.data.models.TwitterUser

@Dao
interface TwitterUserDao {

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getItemById(id: Long): TwitterUser?

    @Query("SELECT * FROM users WHERE screen_name = :screenName")
    suspend fun getItemByScreenName(screenName: String): TwitterUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TwitterUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<TwitterUser>)

    @Query("SELECT * FROM users WHERE logged_user = '1'")
    fun loggedUser(): LiveData<List<TwitterUser>>

    @Query("DELETE FROM users")
    suspend fun clear()
}
