package com.monday8am.tweetmeck.data.local

import androidx.room.TypeConverter
import com.monday8am.tweetmeck.data.models.ListVisibilityMode
import com.monday8am.tweetmeck.data.models.entities.UrlEntity

object RoomConverters {

    const val separator = '\u0000'

    @TypeConverter
    @JvmStatic
    fun fromListVisibilityMode(mode: ListVisibilityMode): String = mode.name

    @TypeConverter
    @JvmStatic
    fun toListVisibilityMode(str: String): ListVisibilityMode = ListVisibilityMode.valueOf(str)

    @TypeConverter
    @JvmStatic
    fun toUrlEntities(str: String): List<UrlEntity> {
        if (str.isEmpty())
            return listOf()
        return str.split(separator).map { UrlEntity.fromEntityString(it) }
    }

    @TypeConverter
    @JvmStatic
    fun fromUrlEntities(entities: List<UrlEntity>): String {
        return entities.map { it.toEntityString() }.fold("", { accumulator, newItem ->
            if(accumulator.isEmpty()) {
                newItem
            } else {
                "${accumulator}${separator}${newItem}"
            }
        })
    }
}
