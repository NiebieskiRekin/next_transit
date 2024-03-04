package com.example.nexttransit

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.datastore.dataStore
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.DynamicThemeColorProviders.background
import androidx.glance.color.DynamicThemeColorProviders.onBackground
import androidx.glance.color.DynamicThemeColorProviders.onPrimaryContainer
import androidx.glance.color.DynamicThemeColorProviders.onSecondaryContainer
import androidx.glance.color.DynamicThemeColorProviders.primaryContainer
import androidx.glance.color.DynamicThemeColorProviders.secondaryContainer
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight.Companion.Bold
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.launch


class TransitWidget : GlanceAppWidget() {


    val Context.dataStore by dataStore("app-settings.json", AppSettingsSerializer)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
//        val store = context.dataStore
//        val initial = store.data

        provideContent {
//            val data by store.data.collectAsState(initial)
            val scope = rememberCoroutineScope()
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
//                SimpleDisplay(data)
                SimpleDisplay()
                Row(modifier = GlanceModifier.fillMaxWidth().padding(4.dp), horizontalAlignment = Alignment.Horizontal.End) {
                    CircleIconButton(
                        ImageProvider(
                            R.drawable.baseline_map_24
                        ),
                        "Open Google Maps",
                        onClick = actionStartActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://www.google.com/maps/dir/?api=1"+
                                    "&origin=o"+
                                    "&origin_place_id=ChIJLcfSImn7BEcRa3MR7sqwJsw"+
                                    "&destination=d"+
                                    "&destination_place_id=ChIJC0kwPxJbBEcRaulLN8Dqppc"+
                                    "&travelmode=transit"
                                )
                            )
                        ),
//                        onClick = {
//                            scope.launch {
////                            val sourcePlaceId = context.dataStore.data.first().lastDirectionsResponse.geocoded_waypoints[0].place_id
////                            val destinationPlaceId = context.dataStore.data.first().lastDirectionsResponse.geocoded_waypoints[1].place_id
//                                val sourcePlaceId = "ChIJLcfSImn7BEcRa3MR7sqwJsw"
//                                val destinationPlaceId = "ChIJC0kwPxJbBEcRaulLN8Dqppc"
//                                var url = "https://www.google.com/maps/dir/?api=1"
//                                url += "&origin=source"
//                                url += "&origin_place_id=$sourcePlaceId"
//                                url += "&destination=destination"
//                                url += "&destination_place_id=$destinationPlaceId"
//                                url += "&travelmode=transit"
//                                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                                mapIntent.setPackage("com.google.android.apps.maps")
//                                startActivity(context, mapIntent, null)
//                            }
//                        },
                        backgroundColor = primaryContainer,
                        contentColor = onPrimaryContainer,
                    )
                    Spacer(GlanceModifier.size(4.dp))
                    CircleIconButton(
                        ImageProvider(
                            R.drawable.baseline_refresh_24_white
                        ),
                        "Refresh",
                        onClick = actionRunCallback<RefreshAction>(),
                        backgroundColor = secondaryContainer,
                        contentColor = onSecondaryContainer,
                    )
                }
            }
        }
    }

    @Composable
    fun MyContent(){
//        val repository = remember { AppSettings() }
        // Retrieve the cache data everytime the content is refreshed
//        val destinations by repository.destinations.collectAsState(State.Loading)

    }

    @Preview(showBackground=true)
    @Composable
    fun SimpleDisplay(directions: DirectionsResponse = ApiCaller.getSampleDirections()){
                when (directions.status) {
                    "OK" -> {
                        Column(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .cornerRadius(20.dp)
                                .background(background)
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(GlanceModifier.fillMaxWidth()) {
                                if (directions.routes.isEmpty()) {
                                    Text(
                                        text = "Error: no route found.",
                                        style = TextStyle(color = onBackground),
                                    )
                                    return@Row
                                }
                            }
                            DisplayRoutes(routes = directions.routes)
                        }
                    }

                    "Error" -> Text(text = "Error: directions data not available.")
                    "Empty" -> Text(text = "")
                    else -> {}
                }
        }

}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // do some work but offset long-term tasks (e.g a Worker)
        TransitWidget().update(context, glanceId)
    }
}


//class SyncService : Service() {
//    override fun onBind(p0: Intent?): IBinder? {
//        TODO()
//    }
//}

@Composable
fun DisplayRoutes(routes: List<Route>) {
    for (route: Route in routes) {
        for (leg: Leg in route.legs) {
            Row(
                horizontalAlignment = Alignment.Start,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column (horizontalAlignment = Alignment.Horizontal.CenterHorizontally){
                    Text(
                        text = leg.start_address.take(20) + "...",
                        maxLines = 1,
                        style = TextStyle(
                            color = onBackground,
                            fontWeight = Bold,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = getLocalTime(leg.departure_time.value),
                        style = TextStyle(
                            color = onBackground,
                            fontWeight = Bold,
                            fontSize = 20.sp
                        )
                    )
                }
                Spacer(GlanceModifier.size(16.dp))
                Column (horizontalAlignment = Alignment.Horizontal.CenterHorizontally){
                    Text(
                        text = leg.end_address.take(20) + "...",
                        maxLines = 1,
                        style = TextStyle(
                            color = onBackground,
                            fontWeight = Bold,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = getLocalTime(leg.arrival_time.value),
                        style = TextStyle(
                            color = onBackground,
                            fontWeight = Bold,
                            fontSize = 20.sp
                        )
                    )
                }
            }
            Row(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                for ((i, bigStep: BigStep) in leg.steps.withIndex()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val travelModeText = getTravelModeText(bigStep)
                        Image(
                            provider = ImageProvider(getTravelModeIconResource(travelModeText)),
//                            provider = ImageProvider(R.drawable.baseline_directions_transit_24_white),
                            contentDescription = travelModeText,
                            colorFilter = ColorFilter.tint(onBackground)
                        )
                        Text(
                            text = getTravelTime(bigStep),
                            style = TextStyle(color = onBackground)
                        )
                    }
                    if (i < leg.steps.lastIndex) {
                        Image(
                            provider = ImageProvider(R.drawable.baseline_chevron_right_24),
                            contentDescription = ">",
                            colorFilter = ColorFilter.tint(onBackground)
                        )
                    }

                }
            }
        }
    }
}

class TransitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TransitWidget()
}

private fun getTravelModeEmoji(travelMode: String) = when (travelMode) {
    "TRANSIT" -> "\uD83D\uDE80"
    "WALKING" -> "\uD83D\uDEB6"
    "BICYCLING" -> "\uD83D\uDEB2"
    "DRIVING" -> "\uD83D\uDE97"
    "BUS" -> "\uD83D\uDE8C"
    "TRAM" -> "\uD83D\uDE8B"
    "HEAVY_RAIL" -> "\uD83D\uDE82"
    "BOAT" -> "⛵"
    else -> "?"
}

private fun getTravelModeIconResource(travelMode: String) = when (travelMode) {
        "TRANSIT" -> R.drawable.baseline_directions_transit_24_white
        "WALKING" -> R.drawable.baseline_directions_walk_24_white
        "BICYCLING" -> R.drawable.baseline_directions_bike_24_white
        "DRIVING" -> R.drawable.baseline_directions_car_24_white
        "BUS" -> R.drawable.baseline_directions_bus_24_white
        "TRAM" -> R.drawable.baseline_tram_24_white
        "HEAVY_RAIL" -> R.drawable.round_train_24_white
        "BOAT" -> R.drawable.baseline_directions_boat_24_white
        else -> R.drawable.baseline_directions_transit_24_white
}


