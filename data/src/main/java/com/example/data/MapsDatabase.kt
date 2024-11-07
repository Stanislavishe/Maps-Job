package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.entity.Points

@Database(
    entities = [Points::class],
    version = 1
)
abstract class MapsDatabase: RoomDatabase() {
    abstract fun mapsDao(): MapsDao
}