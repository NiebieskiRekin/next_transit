package com.example.nexttransit.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.nexttransit.R

/**
 * enum values that represent the screens in the app
 */
enum class AppScreen(@StringRes val title: Int, val icon: ImageVector, @StringRes val contentDescription: Int) {
    Start(title = R.string.app_name, Icons.Default.Home, R.string.app_name),
    WidgetSettings(title = R.string.widget_settings, Icons.Default.Settings, R.string.widget_settings),
    Notifications(title = R.string.notifications, Icons.Default.Notifications, R.string.notifications),
    Calendar(title = R.string.calendar, Icons.Default.CalendarMonth, R.string.calendar)
}