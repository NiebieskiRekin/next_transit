package com.example.nexttransit.model.calendar
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Immutable
import com.example.nexttransit.model.ColorAsIntSerializer
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class CalendarInfo(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val ownerName: String,
    @Serializable(with = ColorAsIntSerializer::class)
    val color: Color? = null
)
