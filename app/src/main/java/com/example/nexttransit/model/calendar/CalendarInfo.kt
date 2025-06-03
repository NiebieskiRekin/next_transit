package com.example.nexttransit.model.calendar
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nexttransit.model.ColorAsIntSerializer
import kotlinx.serialization.Serializable

@Immutable
@Serializable
@Entity
data class CalendarInfo(
    @PrimaryKey(autoGenerate=true)
    val id: Long,
    val displayName: String,
    val accountName: String,
    val ownerName: String,
    @Serializable(with = ColorAsIntSerializer::class)
    val color: Color? = null
)
