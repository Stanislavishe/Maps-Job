package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.entity.Points
import kotlinx.coroutines.flow.Flow

@Dao
interface MapsDao {
    @Query("SELECT * FROM points")
    fun allPoints(): List<Points>

    @Query("SELECT * FROM points WHERE latitude LIKE :latitude LIMIT 1")
    fun getSinglePoint(latitude: Double): Points

    @Insert
    fun insertPoint(point: Points)

    @Delete
    fun deletePoint(point: Points)
}