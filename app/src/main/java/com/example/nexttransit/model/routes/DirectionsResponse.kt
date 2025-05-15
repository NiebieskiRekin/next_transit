package com.example.nexttransit.model.routes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DirectionsResponse(
    @SerialName("geocoded_waypoints")
    val geocodedWaypoints: List<GeocodedWaypoint> = emptyList(),
    val routes: List<Route> = emptyList(),
    val status: String = "",
)