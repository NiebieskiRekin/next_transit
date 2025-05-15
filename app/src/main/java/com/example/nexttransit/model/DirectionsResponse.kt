package com.example.nexttransit.model

import GeocodedWaypoint
import Route
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias PlaceId = String

@Serializable
data class DirectionsResponse(
    @SerialName("geocoded_waypoints")
    val geocodedWaypoints: List<GeocodedWaypoint> = emptyList(),
    val routes: List<Route> = emptyList(),
    val status: String = "",
)