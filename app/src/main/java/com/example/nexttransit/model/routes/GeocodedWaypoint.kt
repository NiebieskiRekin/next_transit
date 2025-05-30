package com.example.nexttransit.model.routes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodedWaypoint(
    @SerialName("geocoder_status")
    val geocoderStatus: String,
    @SerialName("place_id")
    val placeId: PlaceId,
    val types: List<String>,
)