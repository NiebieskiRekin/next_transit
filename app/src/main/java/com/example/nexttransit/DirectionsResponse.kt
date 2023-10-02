package com.example.nexttransit

import kotlinx.serialization.Serializable

@Serializable
data class DirectionsResponse(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String,
){

}

@Serializable
data class GeocodedWaypoint(
    val geocoder_status: String,
    val place_id: String,
    val types: List<String>,
)

@Serializable
data class Route(
    val bounds: Bounds,
    val copyrights: String,
    val legs: List<Leg>,
    val overview_polyline: OverviewPolyline,
    val summary: String,
    val warnings: List<String>,
    val waypoint_order: List<String>
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
    val arrival_time: TimePoint,
    val departure_time: TimePoint,
    val distance: Distance,
    val duration: Duration,
    val end_address: String,
    val end_location: CoordinatesPoint,
    val start_address: String,
    val start_location: CoordinatesPoint,
    val steps: List<BigStep>,
    val traffic_speed_entry: List<String>,
    val via_waypoint: List<String>,
)

@Serializable
data class OverviewPolyline(
    val points: String,
)

@Serializable
data class TimePoint(
    val text: String,
    val time_zone: String,
    val value: Int
)


@Serializable
data class Distance(
    val text: String,
    val value: Int
)

@Serializable
data class Duration(
    val text: String,
    val value: Int
)


@Serializable
data class Step(
    val distance: Distance,
    val duration: Duration,
    val start_location: CoordinatesPoint,
    val end_location: CoordinatesPoint,
    val html_instructions: String,
    val transit_details: TransitDetails,
    val polyline: OverviewPolyline,
    val maneuver: String,
    val travel_mode: String,
)

@Serializable
data class BigStep(
    val distance: Distance,
    val duration: Duration,
    val start_location: CoordinatesPoint,
    val end_location: CoordinatesPoint,
    val html_instructions: String,
    val polyline: OverviewPolyline,
    val transit_details: TransitDetails,
    val travel_mode: String,
    val maneuver: String,
    val steps: List<Step>?
)

@Serializable
data class TransitDetails(
    val arrival_stop: Stop,
    val arrival_time: TimePoint,
    val departure_stop: Stop,
    val departure_time: TimePoint,
    val headsign: String,
    val line: Line,
    val num_stops: Int,
    val trip_short_name: String,
)

@Serializable
data class Stop(
    val location: CoordinatesPoint,
    val name: String
)

@Serializable
data class Line(
    val agencies: List<Agency>,
    val short_name: String,
    val vehicle: Vehicle,
    val color: String,
    val text_color: String,
)

@Serializable
data class Vehicle(
    val type: String,
    val icon: String,
    val name: String
)

@Serializable
data class Agency(
    val phone: String,
    val url: String,
    val name: String
)