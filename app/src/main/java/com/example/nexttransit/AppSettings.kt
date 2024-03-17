package com.example.nexttransit

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings (
    var source: Location = Location(),
    var destination: Location = Location(),
    var lastDirectionsResponse: DirectionsResponse = DirectionsResponse(),
)

@Serializable
data class Location(
    val name: String = "",
    val placeId: PlaceId = "",
)