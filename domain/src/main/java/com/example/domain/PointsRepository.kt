package com.example.domain

import com.example.entity.MapPoint
import com.example.entity.Points
import com.yandex.mapkit.geometry.Point
import javax.inject.Inject

interface PointsRepository {
    fun getAllPoints(): LoadPointResult

    fun getSinglePoint(point: MapPoint): LoadPointResult

    fun deletePoint(point: Points): LoadPointResult

    fun insertPoint(point: Points): LoadPointResult
}