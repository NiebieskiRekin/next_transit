package com.example.nexttransit.model.database

import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.routes.DirectionsResponse

sealed interface DirectionsEvent {
    object SaveDirectionsQuery : DirectionsEvent
    data class SetFirstEvent(val firstEvent: Event) : DirectionsEvent
    data class SetSecondEvent(val secondEvent: Event) : DirectionsEvent
    data class SetDirectionsResponse(val directionsResponse: DirectionsResponse) : DirectionsEvent

}