package com.example.nexttransit

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings (
    val source: Location = Location(),
    val destination: Location = Location(),
    val lastDirectionsResponse: DirectionsResponse = DirectionsResponse(),
)

@Serializable
data class Location(
    val name: String = "",
    val placeId: PlaceId = "",
)