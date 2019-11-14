package com.monday8am.tweetmeck.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.monday8am.tweetmeck.data.models.TwitterList

/**
 * Data Access Object for the TwitterList table.
 */
@Dao
interface TwitterListDao {

    @Query("SELECT * FROM TwitterLists")
    suspend fun getLists(): List<TwitterList>

    @Query("SELECT * FROM TwitterLists WHERE id = :listId")
    suspend fun getListById(listId: Long): TwitterList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(task: TwitterList)

    @Query("DELETE FROM TwitterLists")
    suspend fun deleteLists()

}