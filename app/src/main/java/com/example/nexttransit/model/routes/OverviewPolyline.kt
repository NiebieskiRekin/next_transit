package com.example.nexttransit.model.routes

import kotlinx.serialization.Serializable

@Serializable
data class OverviewPolyline(
    val points: String,
)