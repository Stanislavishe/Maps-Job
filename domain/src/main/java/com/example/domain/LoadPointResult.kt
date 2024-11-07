package com.example.domain

import com.example.entity.Points

interface LoadPointResult {
    fun <T : Any> map(mapper: Mapper<T>): T

    interface Mapper<T : Any> {

        fun mapSingleSuccess(point: Points): T

        fun mapListSuccess(points: List<Points>): T

        fun mapDeleteSuccess(): T

        fun mapInsertSuccess(): T

        fun mapError(message: String): T
    }

    data class SingleSuccess(private val point: Points) : LoadPointResult {
        override fun <T : Any> map(mapper: Mapper<T>): T {
            return mapper.mapSingleSuccess(point)
        }
    }

    data class ListSuccess(private val points: List<Points>) : LoadPointResult {
        override fun <T : Any> map(mapper: Mapper<T>): T {
            return mapper.mapListSuccess(points)
        }
    }
    data object DeleteSuccess : LoadPointResult {
        override fun <T : Any> map(mapper: Mapper<T>): T {
            return mapper.mapDeleteSuccess()
        }
    }
    data object InsertSuccess : LoadPointResult {
        override fun <T : Any> map(mapper: Mapper<T>): T {
            return mapper.mapInsertSuccess()
        }
    }

    data class Error(private val error: String) : LoadPointResult {
        override fun <T : Any> map(mapper: Mapper<T>): T {
            return mapper.mapError(error)
        }
    }
}