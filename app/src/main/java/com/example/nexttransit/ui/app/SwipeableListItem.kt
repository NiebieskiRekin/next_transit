package com.example.nexttransit.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableListItem(
    content: @Composable () -> Unit,
    onDelete: () -> Unit,
    addToCalendar: () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToEnd -> {
                    addToCalendar()
                    true
                }

                DismissValue.DismissedToStart -> {
                    onDelete()
                    true
                }

                else -> false
            }
        }
    )

    if (dismissState.isDismissed(DismissDirection.EndToStart)) {
        // Delete item and reset state
        LaunchedEffect(key1 = content) {
            onDelete()
//            dismissState.snapTo(DismissValue.Default)
        }
    }

    if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
        LaunchedEffect(key1 = content) {
            addToCalendar()
            dismissState.snapTo(DismissValue.Default)
        }
    }

    val swipeFraction = dismissState.progress.fraction
    if (abs(swipeFraction) < 0.3f && (!dismissState.isDismissed(DismissDirection.EndToStart) || !dismissState.isDismissed(
            DismissDirection.StartToEnd
        ))
    ) {
        LaunchedEffect(key1 = content) {
            dismissState.snapTo(DismissValue.Default)
        }
    }


    SwipeToDismiss(
        state = dismissState,
        background = {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Add to calendar",
                    tint = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                content()
            }
        }
    )
}
