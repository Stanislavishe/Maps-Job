package com.example.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MapPoint(
    val latitude: Double,
    val longitude: Double
): Parcelable