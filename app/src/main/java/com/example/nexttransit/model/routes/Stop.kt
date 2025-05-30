package com.example.nexttransit.model.routes

import kotlinx.serialization.Serializable

@Serializable
data class Stop(
    val location: CoordinatesPoint,
    val name: String
)