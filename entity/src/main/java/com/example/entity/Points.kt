package com.example.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "points")
data class Points(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
)