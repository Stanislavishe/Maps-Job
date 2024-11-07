package com.example.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.LoadPointResult
import com.example.domain.PointsRepository
import com.example.entity.MapPoint
import com.example.entity.Points
import com.example.presentation.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val pointsRepository: PointsRepository,
    private val mapper: LoadPointResult.Mapper<UIState>
): ViewModel() {

    private val _allPoints = MutableStateFlow<UIState>(UIState.Initial)
    val allPoints = _allPoints.asStateFlow()

    private val _insertPoint = MutableStateFlow<UIState>(UIState.Initial)
    val insertPoint = _insertPoint.asStateFlow()

    private val _deletePoint = MutableStateFlow<UIState>(UIState.Initial)
    val deletePoint = _deletePoint.asStateFlow()

    private val _pointToDelete = MutableStateFlow<MapPoint?>(null)
    val pointToDelete = _pointToDelete.asStateFlow()

    private val _singlePoint = MutableStateFlow<UIState>(UIState.Initial)
    val singlePoint = _singlePoint.asStateFlow()

    private val _routePoint = MutableStateFlow<MapPoint?>(null)
    val routePoint = _routePoint.asStateFlow()

    fun getAllPoints() {
        viewModelScope.launch(Dispatchers.IO) {
            val uiState = pointsRepository.getAllPoints().map(mapper)
            _allPoints.value = uiState
        }
    }

    fun shareRoutePoint(point: MapPoint) {
        _routePoint.value = point
    }

    fun pointToDelete(mapPoint: MapPoint) {
        _pointToDelete.value = mapPoint
    }

    fun deletePoint(point: Points) {
        viewModelScope.launch(Dispatchers.IO) {
            val uiState = pointsRepository.deletePoint(point).map(mapper)
            _deletePoint.value = uiState
        }
    }

    fun getSinglePoint(point: MapPoint) {
        viewModelScope.launch(Dispatchers.IO) {
            val uiState = pointsRepository.getSinglePoint(point).map(mapper)
            _singlePoint.value = uiState
        }
    }

    fun insertPoint(point: Points) {
        viewModelScope.launch(Dispatchers.IO) {
            val uiState = pointsRepository.insertPoint(point).map(mapper)
            _insertPoint.value = uiState
        }
    }
}