package com.example.nexttransit.model.routes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Leg(
    @SerialName("arrival_time")
    val arrivalTime: TimePoint,
    @SerialName("departure_time")
    val departureTime: TimePoint,
    val distance: Distance,
    val duration: Duration,
    @SerialName("end_address")
    val endAddress: String,
    @SerialName("end_location")
    val endLocation: CoordinatesPoint,
    @SerialName("start_address")
    val startAddress: String,
    @SerialName("start_location")
    val startLocation: CoordinatesPoint,
    val steps: List<Step>,
    @SerialName("traffic_speed_entry")
    val trafficSpeedEntry: List<String>,
    @SerialName("via_waypoint")
    val viaWaypoint: List<String>,
)