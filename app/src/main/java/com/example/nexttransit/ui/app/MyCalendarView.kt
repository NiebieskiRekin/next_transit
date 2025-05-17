package com.example.nexttransit.ui.app

import android.Manifest
import android.content.ContentResolver
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.nexttransit.model.calendar.CalendarInfo
import com.example.nexttransit.model.calendar.getAvailableCalendars
import com.example.nexttransit.model.calendar.getEvents
import java.time.LocalDate
import java.time.ZoneId

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MyCalendarView(contentResolver: ContentResolver) {
    // In your Activity or Composable
    var events = remember { mutableStateListOf<Event>() }
    var calendar by remember { mutableStateOf<CalendarInfo?>(null) }
    val calendars = remember { mutableStateListOf<CalendarInfo>() }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. You can proceed to fetch calendars/events.
            Log.d("CalendarAccess", "READ_CALENDAR permission granted.")
            // Launch your calendar fetching logic here
            calendars.clear()
            calendars.addAll(getAvailableCalendars(contentResolver))
        } else {
            // Permission denied. Handle appropriately (e.g., show a message).
            Log.w("CalendarAccess", "READ_CALENDAR permission denied.")
            // Inform the user why the permission is needed
            events.clear()
        }
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState()
        ) // https://developer.android.com/develop/ui/compose/components/app-bars#scroll
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    fun onDismissRequest() {
        events.clear()
        Log.d("CalendarAccess", "No calendar chosen")
    }

    fun onDateChanged() {
        if (calendar != null) {
            events.clear()
            val startDateMillis =
                selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli()
            val endDateMillis = selectedDate.atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            events.addAll(
                getEvents(
                    contentResolver,
                    calendar!!.id,
                    startDateMillis,
                    endDateMillis
                ).toMutableStateList()
            )
            Log.d("CalendarAccess", events.toString())
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    SimpleCalendarView(onDateSelected = { date ->
                        selectedDate = date
                        onDateChanged()
                        Log.d("MainActivity", "Selected date: $date")
                    })
                },
                scrollBehavior = scrollBehavior,
                expandedHeight = 400.dp
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        DailyScheduleView(
            selectedDate,
            events.toList(),
            Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        )
    }

    if (calendar == null) {
        Dialog(onDismissRequest = { onDismissRequest() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(375.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (calendars.isEmpty()) {
                        Text("No calendars available")
                        Row {
                            TextButton(
                                onClick = { onDismissRequest() },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text("Dismiss", textAlign = TextAlign.Center)
                            }
                            TextButton(
                                onClick = {
                                    requestPermissionLauncher.launch(
                                        Manifest.permission.READ_CALENDAR
                                    )
                                },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text(
                                    "Grant access to calendars",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Text("Please choose a calendar")

                        // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
                        LazyColumn(Modifier.selectableGroup()) {
                            itemsIndexed(calendars) { i, c ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = (calendar == c),
                                            onClick = {
                                                calendar = c; onDateChanged()
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (c == calendar),
                                        onClick = null
                                    )
                                    Text(
                                        text = c.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}