package com.example.nexttransit.model

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

@Serializable
data class GeocodedWaypoint(
    @SerialName("geocoder_status")
    val geocoderStatus: String,
    @SerialName("place_id")
    val placeId: PlaceId,
    val types: List<String>,
)

@Serializable
data class Route(
    val bounds: Bounds,
    val copyrights: String,
    val legs: List<Leg>,
    @SerialName("overview_polyline")
    val overviewPolyline: OverviewPolyline,
    val summary: String,
    val warnings: List<String>,
    @SerialName("waypoint_order")
    val waypointOrder: List<String>
)

@Serializable
data class Bounds(
    val northeast: CoordinatesPoint,
    val southwest: CoordinatesPoint,
)

@Serializable
data class CoordinatesPoint(
    val lat: Float,
    val lng: Float,
)

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

@Serializable
data class OverviewPolyline(
    val points: String,
)

@Serializable
data class TimePoint(
    val text: String,
    @SerialName("time_zone")
    val timeZone: String,
    val value: Long,
)


@Serializable
data class Distance(
    val text: String,
    val value: Int
)

@Serializable
data class Duration(
    val text: String,
    val value: Long
)

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

@Serializable
data class TransitDetails(
    @SerialName("arrival_stop")
    val arrivalStop: Stop,
    @SerialName("arrival_time")
    val arrivalTime: TimePoint,
    @SerialName("departure_stop")
    val departureStop: Stop,
    @SerialName("departure_time")
    val departureTime: TimePoint,
    val headsign: String,
    val line: Line?=null,
    @SerialName("num_stops")
    val numStops: Int,
    @SerialName("trip_short_name")
    val tripShortName: String="",
)

@Serializable
data class Stop(
    val location: CoordinatesPoint,
    val name: String
)

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

@Serializable
data class Vehicle(
    val type: String,
    val icon: String,
    val name: String
)

@Serializable
data class Agency(
    val phone: String?=null,
    val url: String?=null,
    val name: String
)