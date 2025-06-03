package com.example.nexttransit.model.database

import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.database.classes.DepartAtOrArriveBy
import com.example.nexttransit.model.database.classes.DirectionsQuery
import com.example.nexttransit.model.routes.DirectionsResponse

data class DirectionsState(
    val directions: List<DirectionsQuery> = emptyList(),
    val firstEvent: Event = Event(),
    val secondEvent: Event = Event(),
    val departAtOrArriveBy: DepartAtOrArriveBy = DepartAtOrArriveBy.DepartAt,
    val directionsResponse: DirectionsResponse = DirectionsResponse(),
    val selectedDirections: DirectionsQuery? = null
)