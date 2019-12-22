package com.monday8am.tweetmeck.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.monday8am.tweetmeck.data.models.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Session)

    @Query("SELECT * FROM sessions")
    fun currentSessionFlow(): Flow<List<Session>>

    @Query("DELETE FROM sessions")
    suspend fun clear()
}
