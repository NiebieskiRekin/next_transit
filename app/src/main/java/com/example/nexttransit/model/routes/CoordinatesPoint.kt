package com.example.nexttransit.model.routes

import kotlinx.serialization.Serializable

@Serializable
data class CoordinatesPoint(
    val lat: Float,
    val lng: Float,
)