package com.example.nexttransit

import android.text.format.DateFormat
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