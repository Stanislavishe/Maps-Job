package com.example.presentation

import com.example.domain.LoadPointResult
import com.example.domain.LoadPointResult.Mapper
import com.example.entity.Points

interface UIState {
    data class SingleSuccess(val point: Points) : UIState

    data class ListSuccess(val points: List<Points>) : UIState

    data object DeleteSuccess : UIState

    data object InsertSuccess : UIState

    data class Error(val error: String) : UIState

    data object Initial: UIState
}