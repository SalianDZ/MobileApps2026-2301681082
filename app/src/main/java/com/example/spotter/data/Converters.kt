package com.example.spotter.data

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromTagsList(tags: List<String>): String {
        return tags.joinToString(separator = ",")
    }

    @TypeConverter
    fun toTagsList(tagsString: String): List<String> {
        if (tagsString.isEmpty()) return emptyList()
        return tagsString.split(",").map { it.trim() }
    }
}