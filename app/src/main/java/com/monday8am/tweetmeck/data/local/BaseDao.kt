package com.monday8am.tweetmeck.data.local

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.monday8am.tweetmeck.data.models.TwitterList

interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg obj: T)

    @Query("SELECT * FROM TwitterLists WHERE id = :listId")
    suspend fun getListById(listId: Long): TwitterList?

    fun deleteById(id: Long)


}