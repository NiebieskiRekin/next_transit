package com.example.nexttransit.model.settings

import com.example.nexttransit.api.ApiCaller
import com.example.nexttransit.model.routes.DirectionsResponse
import com.example.nexttransit.model.routes.Location
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

      fun getDefault() = AppSettings(
            lastDirectionsResponse = ApiCaller.getSampleDirections(),
            source = Location("Środa Wielkopolska", "ChIJLcfSImn7BEcRa3MR7sqwJsw"),
            destination = Location("Politechnika Poznańska, Kampus Piotrowo","ChIJC0kwPxJbBEcRaulLN8Dqppc")
        )

}