package com.example.nexttransit.model.calendar
import androidx.compose.runtime.Immutable

@Immutable
data class CalendarInfo(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val ownerName: String,
    val color: Int? = null // Calendar color
)
