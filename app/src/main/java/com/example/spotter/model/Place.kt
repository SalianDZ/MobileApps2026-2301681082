package com.example.spotter.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places_table")
data class Place(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imagePath: String? = null,
    val tags: List<String> = emptyList()
)