package com.example.nexttransit.ui.app

import com.example.nexttransit.model.routes.Leg
import com.example.nexttransit.model.routes.Route
import com.example.nexttransit.model.routes.Step
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.example.nexttransit.model.routes.DirectionsResponse
import com.example.nexttransit.api.ApiCaller.getSampleDirections
import com.example.nexttransit.getTravelModeIcon
import com.example.nexttransit.getTravelModeText
import com.example.nexttransit.getTravelTime
import com.example.nexttransit.ui.theme.NextTransitTheme
import kotlin.collections.forEach
import android.net.Uri
import android.util.Log


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



@Composable
fun LoadingDirectionsWidget(
    directions: DirectionsResponse,
    source: String,
    destination: String,
    directionsButtonClicked: Boolean,
    directionsGenerated: Boolean
) {
    if (!directionsGenerated && directionsButtonClicked) {
        ColumnPill(Modifier.height(80.dp)) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onSecondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    } else if (directionsGenerated) {
        DirectionsWidget(
            directions,
            source,
            destination
        )
    }
}


@Composable
fun DirectionsWidget(
    directions: DirectionsResponse,
    source: String,
    destination: String
) {
    NextTransitTheme {
        when (directions.status) {
            "OK" -> {

                val context = LocalContext.current
                ColumnPill(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            val originEncoded = Uri.encode(source)
                            val destinationEncoded = Uri.encode(destination)

                            // Używamy bezpośrednio czasu odjazdu z API
                            val departureTimestampRaw = directions.routes.firstOrNull()
                                ?.legs?.firstOrNull()?.departureTime?.value ?: 0L

                            val departureTimestamp = if (departureTimestampRaw > 1000000000000L) {
                                departureTimestampRaw / 1000
                            } else {
                                departureTimestampRaw
                            }

                            val mapsUrl = "https://www.google.com/maps/dir/?api=1" +
                                    "&origin=$originEncoded" +
                                    "&destination=$destinationEncoded" +
                                    "&travelmode=transit" +
                                    "&dirflg=r" +
                                    "&departure_time=$departureTimestamp" // Zmieniono na departureTimestamp

                            Log.d("GOOGLEMAPS", "Generated link: $mapsUrl")

                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                            context.startActivity(intent)
                        }
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

@Preview
@Composable
fun LoadingDirectionsWidgetPreview(
    directions: DirectionsResponse = getSampleDirections(),
    source: String = "Kraków ul. Wschodnia 5",
    destination: String = "Poznań, Piotrowo 2",
    directionsButtonClicked: Boolean = true,
    directionsGenerated: Boolean = false
) {
    NextTransitTheme {
        LoadingDirectionsWidget(directions,source,destination,directionsButtonClicked,directionsGenerated)
    }
}

@Preview()
@Composable
private fun DirectionsWidgetPreview(
    directions: DirectionsResponse = getSampleDirections(),
    source: String = "Kraków ul. Wschodnia 5",
    destination: String = "Poznań, Piotrowo 2",
) {
    NextTransitTheme {
        DirectionsWidget(directions,source,destination)
    }
}