package com.example.nexttransit.model.routes

import kotlinx.serialization.Serializable

@Serializable
data class Bounds(
    val northeast: CoordinatesPoint,
    val southwest: CoordinatesPoint,
)