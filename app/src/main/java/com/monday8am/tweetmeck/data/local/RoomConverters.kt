package com.monday8am.tweetmeck.data.local

import androidx.room.TypeConverter
import com.monday8am.tweetmeck.data.models.ListVisibilityMode

object RoomConverters {

    @TypeConverter
    @JvmStatic
    fun fromListVisibilityMode(mode: ListVisibilityMode): String = mode.name

    @TypeConverter
    @JvmStatic
    fun toListVisibilityMode(str: String): ListVisibilityMode = ListVisibilityMode.valueOf(str)
}
