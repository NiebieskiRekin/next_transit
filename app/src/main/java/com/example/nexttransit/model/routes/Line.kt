package com.example.nexttransit.model.routes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Line(
    val agencies: List<Agency>,
    @SerialName("short_name")
    val shortName: String="",
    val vehicle: Vehicle,
    val color: String="",
    @SerialName("text_color")
    val textColor: String="",
    val name: String="",
)