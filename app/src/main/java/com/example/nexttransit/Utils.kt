package com.example.nexttransit

import android.text.format.DateFormat
import com.example.nexttransit.model.routes.Step
import java.util.Calendar
import java.util.Locale

fun getLocalTime(ts: Long?): String {
    if (ts == null) return ""
    //Get instance of calendar
    val calendar = Calendar.getInstance(Locale.getDefault())
    //get current date from ts
    calendar.timeInMillis = ts * 1000
    //return formatted date
    return DateFormat.format("HH:mm", calendar).toString()
}

fun getTravelModeText(bigStep: Step): String {
    return if (bigStep.travelMode == "TRANSIT") {
        bigStep.transitDetails?.line?.vehicle?.type ?: "TRANSIT"
    } else {
        bigStep.travelMode
    }
}

fun getTravelTime(bigStep: Step): String {
    return if (bigStep.travelMode == "TRANSIT") {
        (getLocalTime(bigStep.transitDetails?.departureTime?.value) + "-" + getLocalTime(bigStep.transitDetails?.arrivalTime?.value))
    } else {
        bigStep.duration.text
    }
}

fun getTravelModeIconResource(travelMode: String) = when (travelMode) {
    "TRANSIT" -> R.drawable.baseline_directions_transit_24_white
    "WALKING" -> R.drawable.baseline_directions_walk_24_white
    "BICYCLING" -> R.drawable.baseline_directions_bike_24_white
    "DRIVING" -> R.drawable.baseline_directions_car_24_white
    "BUS" -> R.drawable.baseline_directions_bus_24_white
    "TRAM" -> R.drawable.baseline_tram_24_white
    "HEAVY_RAIL" -> R.drawable.round_train_24_white
    "BOAT" -> R.drawable.baseline_directions_boat_24_white
    else -> R.drawable.baseline_directions_transit_24_white
}