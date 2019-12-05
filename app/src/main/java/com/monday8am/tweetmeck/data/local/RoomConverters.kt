package com.monday8am.tweetmeck.data.local

import androidx.room.TypeConverter
import com.monday8am.tweetmeck.data.models.ListVisibilityMode
import com.monday8am.tweetmeck.util.toOffsetDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object RoomConverters {

    @TypeConverter
    @JvmStatic
    fun fromListVisibilityMode(mode: ListVisibilityMode): String = mode.name

    @TypeConverter
    @JvmStatic
    fun toListVisibilityMode(str: String): ListVisibilityMode = ListVisibilityMode.valueOf(str)

    @TypeConverter
    @JvmStatic
    fun toOffsetDateTime(value: String): OffsetDateTime {
        return value.toOffsetDateTime()
    }

    @TypeConverter
    @JvmStatic
    fun fromOffsetDateTime(date: OffsetDateTime): String {
        return date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}
