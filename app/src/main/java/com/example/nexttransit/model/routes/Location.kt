package com.example.nexttransit.model.routes

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val name: String = "",
    val placeId: PlaceId = "",
)