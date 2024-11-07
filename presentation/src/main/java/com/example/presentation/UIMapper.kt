package com.example.presentation

import com.example.domain.LoadPointResult
import com.example.entity.Points
import javax.inject.Inject

class UIMapper @Inject constructor(): LoadPointResult.Mapper<UIState> {
    override fun mapSingleSuccess(point: Points): UIState {
        return UIState.SingleSuccess(point)
    }

    override fun mapListSuccess(points: List<Points>): UIState {
        return UIState.ListSuccess(points)
    }

    override fun mapDeleteSuccess(): UIState {
        return UIState.DeleteSuccess
    }

    override fun mapInsertSuccess(): UIState {
        return UIState.InsertSuccess
    }

    override fun mapError(message: String): UIState {
        return UIState.Error(message)
    }
}