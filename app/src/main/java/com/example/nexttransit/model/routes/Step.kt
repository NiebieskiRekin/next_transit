package com.example.nexttransit.model.routes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Step(
    val distance: Distance,
    val duration: Duration,
    @SerialName("start_location")
    val startLocation: CoordinatesPoint,
    @SerialName("end_location")
    val endLocation: CoordinatesPoint,
    @SerialName("html_instructions")
    val htmlInstructions: String,
    val polyline: OverviewPolyline,
    @SerialName("transit_details")
    val transitDetails: TransitDetails?=null,
    @SerialName("travel_mode")
    val travelMode: String,
    val maneuver: String="",
    val steps: List<Step>? = null
)