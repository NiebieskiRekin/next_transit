package com.example.nexttransit.ui.app

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexttransit.api.ApiCaller.getSampleDirections
import com.example.nexttransit.api.ApiCaller.trimPolyline
import com.example.nexttransit.model.routes.DirectionsResponse
import com.example.nexttransit.model.settings.AppSettings

@Composable
fun DebugOutput(
    appSettings: AppSettings = AppSettings().getDefault(),
    newDirections: DirectionsResponse = getSampleDirections(),
) {
    var showDebugOutput by remember { mutableStateOf(false) }
    Column {
        TextButton(onClick = { showDebugOutput = !showDebugOutput }, content = {
            Text(
                if (showDebugOutput) {
                    "Hide Debug Output"
                } else {
                    "Show Debug Output"
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Sharp.ChevronRight,
                contentDescription = ">",
                Modifier.rotate(
                    if (showDebugOutput) {
                        90f
                    } else {
                        0f
                    }
                )
            )
        },
            modifier = Modifier.padding(5.dp, 0.dp)
        )
        if (showDebugOutput) {
            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .border(2.dp, Color.LightGray)
                    .padding(5.dp)
                    .height(700.dp)
            ) {
                item {
                    Text(
                        text = "Saved origin 1:",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    SelectionContainer {
                        Text(text = appSettings.source.toString())
                    }
                }
                item {
                    Spacer(
                        Modifier
                            .size(0.dp, 5.dp)
                            .fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = "Saved destination 1:",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    SelectionContainer {
                        Text(text = appSettings.destination.toString())
                    }
                }
                item {
                    Spacer(
                        Modifier
                            .size(0.dp, 5.dp)
                            .fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = "Saved last directions response 1:",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    SelectionContainer {
                        Text(text = trimPolyline(appSettings.lastDirectionsResponse).toString())
                    }
                }
                item {
                    Spacer(
                        Modifier
                            .size(0.dp, 10.dp)
                            .fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = "New Directions 1:",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    SelectionContainer {
                        Text(text = trimPolyline(newDirections).toString())
                    }
                }
                item {
                    Spacer(
                        Modifier
                            .size(0.dp, 15.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}