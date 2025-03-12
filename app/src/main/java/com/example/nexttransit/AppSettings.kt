package com.example.nexttransit

import com.example.nexttransit.ApiCaller.getSampleDirections
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings (
    var source: Location = Location(),
    var destination: Location = Location(),
    var lastDirectionsResponse: DirectionsResponse = DirectionsResponse(),
    var secondSource: Location = destination,
    var secondDestination: Location = source,
    var returnResponse: DirectionsResponse = DirectionsResponse()
) {
    fun getSource(i: Int) : Location{
        return if (i % 2 == 0){
            source
        } else {
            secondSource
        }
    }

    fun setSource(i: Int, newValue: Location) {
        if (i % 2 == 0){
            source = newValue
        } else {
            secondSource = newValue
        }
    }

    fun getDestination(i: Int) : Location{
        return if (i % 2 == 0){
            destination
        } else {
            secondDestination
        }
    }

    fun setDestination(i: Int, newValue: Location) {
        if (i % 2 == 0){
            destination = newValue
        } else {
            secondDestination = newValue
        }
    }

    fun getDirections(i: Int) : DirectionsResponse{
        return if (i % 2 == 0){
            lastDirectionsResponse
        } else {
            returnResponse
        }
    }

    fun setDirections(i: Int, newValue: DirectionsResponse) {
        if (i % 2 == 0){
            lastDirectionsResponse = newValue
        } else {
            returnResponse = newValue
        }
    }

      fun getDefault() = AppSettings(
            lastDirectionsResponse = getSampleDirections(),
            source = Location("Środa Wielkopolska", "ChIJLcfSImn7BEcRa3MR7sqwJsw"),
            destination = Location("Politechnika Poznańska, Kampus Piotrowo","ChIJC0kwPxJbBEcRaulLN8Dqppc")
        )

}

@Serializable
data class Location(
    val name: String = "",
    val placeId: PlaceId = "",
)


