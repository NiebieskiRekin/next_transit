package com.example.nexttransit

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DirectionsRailway
import androidx.compose.material.icons.rounded.DirectionsTransit
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Tram
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.example.nexttransit.ApiCaller.getSampleDirections
import com.example.nexttransit.ui.theme.NextTransitTheme


private fun getTravelModeIcon(travelMode: String) = when (travelMode) {
    "TRANSIT" -> Icons.Rounded.DirectionsTransit
    "WALKING" -> Icons.AutoMirrored.Rounded.DirectionsWalk
    "BICYCLING" -> Icons.AutoMirrored.Rounded.DirectionsBike
    "DRIVING" -> Icons.Rounded.DirectionsCar
    "BUS" -> Icons.Rounded.DirectionsBus
    "TRAM" -> Icons.Rounded.Tram
    "HEAVY_RAIL" -> Icons.Rounded.DirectionsRailway
    "BOAT" -> Icons.Rounded.DirectionsBoat
    else -> Icons.Rounded.QuestionMark
}

@Composable
fun ColumnPill(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(10.dp)
    ) {
        content()
    }
}

@Composable
fun ShowError(text: String) {
    ColumnPill(modifier = Modifier.height(120.dp)) {
        Text(
            "Error: ",
            fontWeight = FontWeight.Bold,
        )
        Text(
            text,
        )
    }
}


@Preview
@Composable
fun LoadingDirectionsWidget(
    directions: DirectionsResponse = getSampleDirections(),
    source: String = "Środa Wlkp.",
    destination: String = "Poznań, Piotrowo 2",
    directionsButtonClicked: Boolean = true,
    directionsGenerated: Boolean = false
) {
    if (!directionsGenerated && directionsButtonClicked) {
        ColumnPill(Modifier.height(80.dp)) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onSecondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    } else if (directionsGenerated) {
        SimpleDisplay(
            directions,
            source,
            destination
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun SimpleDisplay(
    directions: DirectionsResponse = getSampleDirections(),
    source: String = "Środa Wlkp.",
    destination: String = "Poznań"
) {
    NextTransitTheme {
        when (directions.status) {
            "OK" -> {
                ColumnPill(
                    modifier = Modifier.clickable(true, "Open Google Maps", null, onClick =
                        {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                ("https://www.google.com/maps/dir/?api=1" +
                                        "&origin=${source}" +
                                        "&destination=${destination}" +
                                        "&travelmode=transit").toUri()
                            )
                            // TODO start main activity
//                            ContextCompat.startActivity(MainActivity::class, intent, null)
                        })
                ) {
                    if (directions.routes.isEmpty()) {
                        ShowError(text = "No route found.")
                    }
                    directions.routes.forEach { route: Route ->
                        route.legs.forEach { leg: Leg ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Departure: ",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = leg.departureTime.text,
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Spacer(Modifier.size(16.dp))
                                Text(
                                    text = "Planned Arrival: ",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = leg.arrivalTime.text,
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                            LazyRow(
                                verticalAlignment = Alignment.Top,
                            ) {
                                for ((i, bigStep: Step) in leg.steps.withIndex()) {
                                    item {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            val travelModeText = getTravelModeText(bigStep)
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Icon(
                                                    imageVector = getTravelModeIcon(
                                                        travelModeText
                                                    ),
                                                    travelModeText,
                                                    tint = MaterialTheme.colorScheme.onSecondary,
                                                )
                                                if (bigStep.transitDetails?.line != null) {
                                                    val text =
                                                        if (bigStep.transitDetails.line.shortName.isNotBlank()) {
                                                            bigStep.transitDetails.line.shortName
                                                        } else if (bigStep.transitDetails.line.name.isNotBlank()) {
                                                            bigStep.transitDetails.line.name
                                                        } else {
                                                            return@Row
                                                        }
                                                    val textColor =
                                                        if (bigStep.transitDetails.line.textColor.isNotBlank()) {
                                                            Color(bigStep.transitDetails.line.textColor.toColorInt())
                                                        } else {
                                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                        }
                                                    val backgroundTextColor =
                                                        if (bigStep.transitDetails.line.color.isNotBlank()) {
                                                            Color(bigStep.transitDetails.line.color.toColorInt())
                                                        } else {
                                                            MaterialTheme.colorScheme.primaryContainer
                                                        }
                                                    Text(
                                                        text = text,
                                                        style = TextStyle(color = textColor),
                                                        modifier = Modifier
                                                            .clip(MaterialTheme.shapes.small)
                                                            .background(backgroundTextColor)
                                                            .padding(2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = getTravelTime(bigStep),
                                                style = TextStyle(color = MaterialTheme.colorScheme.onSecondary)
                                            )
                                        }
                                        if (i < leg.steps.lastIndex) {
                                            Icon(
                                                imageVector = Icons.Rounded.ChevronRight,
                                                ">",
                                                tint = MaterialTheme.colorScheme.onSecondary,
                                            )
//                                        Spacer(Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Error" -> ShowError(text = "Directions data not available.")
            "Empty" -> ShowError(text = "Empty response.")
            else -> {}
        }
    }
}