package com.example.spotter.data // Внимавай за твоя пакет!

import androidx.room.TypeConverter

class Converters {

    // Превръща списъка ["планина", "лято"] в текст "планина,лято"
    @TypeConverter
    fun fromTagsList(tags: List<String>): String {
        return tags.joinToString(separator = ",")
    }

    // Превръща текста "планина,лято" обратно в списък ["планина", "лято"]
    @TypeConverter
    fun toTagsList(tagsString: String): List<String> {
        if (tagsString.isEmpty()) return emptyList()
        return tagsString.split(",").map { it.trim() }
    }
}