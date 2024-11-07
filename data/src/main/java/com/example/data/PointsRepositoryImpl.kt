package com.example.data

import android.util.Log
import com.example.domain.LoadPointResult
import com.example.domain.PointsRepository
import com.example.entity.MapPoint
import com.example.entity.Points
import com.yandex.mapkit.geometry.Point
import javax.inject.Inject

class PointsRepositoryImpl @Inject constructor(
    private val dao: MapsDao
) : PointsRepository {
    override fun getAllPoints(): LoadPointResult {
        return try {
            val points = dao.allPoints()
            LoadPointResult.ListSuccess(points)
        } catch (e: Exception) {
            LoadPointResult.Error(e.message ?: "")
        }
    }

    override fun getSinglePoint(point: MapPoint): LoadPointResult {
        return try {
            val returnedPoint = dao.getSinglePoint(point.latitude)
            LoadPointResult.SingleSuccess(returnedPoint)
        } catch (e: Exception) {
            LoadPointResult.Error(e.message ?: "")
        }
    }

    override fun deletePoint(point: Points): LoadPointResult {
        return try {
            dao.deletePoint(point)
            LoadPointResult.DeleteSuccess
        } catch (e: Exception) {
            LoadPointResult.Error(e.message ?: "")
        }
    }

    override fun insertPoint(point: Points): LoadPointResult {
        return try {
            dao.insertPoint(point)
            LoadPointResult.InsertSuccess
        } catch (e: Exception) {
            LoadPointResult.Error(e.message ?: "")
        }
    }
}