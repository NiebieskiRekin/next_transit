package com.example.nexttransit

import android.content.Context
import android.content.Intent
import android.graphics.Color.parseColor
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.DynamicThemeColorProviders
import androidx.glance.color.DynamicThemeColorProviders.background
import androidx.glance.color.DynamicThemeColorProviders.onBackground
import androidx.glance.color.DynamicThemeColorProviders.onPrimaryContainer
import androidx.glance.color.DynamicThemeColorProviders.onSecondaryContainer
import androidx.glance.color.DynamicThemeColorProviders.secondaryContainer
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight.Companion.Bold
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class TransitWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = TransitWidgetStateDefinition()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("Widget", "provideGlance")

        provideContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        val context = LocalContext.current
        val appSettings by WidgetSettingsRepo.currentSettings.collectAsState()
        Log.d("TransitWidget", "Content redraw")
        Log.d("TransitWidget",appSettings.toString())

        val directions = appSettings.lastDirectionsResponse
        Log.d("GlanceWidget", directions.toString())
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(background)
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.Top,
        ) {
            when (directions.status) {
                "OK" -> {
                    if (directions.routes.isEmpty()) {
                        Text(
                            text = "Error: no route found.",
                            style = TextStyle(color = onBackground),
                        )
                    }
                    DisplayRoutes(
                        routes = directions.routes,
                        appSettings.source.placeId, appSettings.destination.placeId, context
                    )
                }

                "Error" -> Text(
                    text = "Error: directions data not available.",
                    style = TextStyle(color = onBackground),
                    modifier = GlanceModifier.fillMaxSize()
                )

                "" -> Text(
                    text = "Empty",
                    style = TextStyle(color = onBackground),
                    modifier = GlanceModifier.fillMaxSize()
                )

                else -> {}
            }
        }
    }

    @Composable
    fun DisplayRoutes(
        routes: List<Route>,
        sourcePlaceId: PlaceId,
        destinationPlaceId: PlaceId,
        context: Context
    ) {
        routes.forEachIndexed { i: Int, route: Route ->
            route.legs.forEachIndexed { j: Int, leg: Leg ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.Top
                ) {
                    Row(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .clickable(
                                actionStartActivity(
                                    Intent(context, MainActivity::class.java)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                            modifier = GlanceModifier.defaultWeight().padding(4.dp, 0.dp)
                        ) {
                            Text(
                                text = leg.startAddress,
//                            text = makeEllipsis(leg.startAddress),
                                maxLines = 2,
                                style = TextStyle(
                                    color = onPrimaryContainer,
                                    fontWeight = Bold,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                ),
                            )
                            Text(
                                text = getLocalTime(leg.departureTime.value),
                                style = TextStyle(
                                    color = onPrimaryContainer,
                                    fontWeight = Bold,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                            modifier = GlanceModifier.defaultWeight().padding(4.dp, 0.dp)
                        ) {
                            Text(
                                text = leg.endAddress,
//                            text = makeEllipsis(leg.endAddress),
                                maxLines = 2,
                                style = TextStyle(
                                    color = onPrimaryContainer,
                                    fontWeight = Bold,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                ),
                            )
                            Text(
                                text = getLocalTime(leg.arrivalTime.value),
                                style = TextStyle(
                                    color = onPrimaryContainer,
                                    fontWeight = Bold,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                ),
                            )
                        }
                    }
                    if (i == 0 && j == 0) {
                        CircleIconButton(
                            ImageProvider(
                                R.drawable.baseline_map_24
                            ),
                            "Open Google Maps",
                            onClick = actionStartActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        "https://www.google.com/maps/dir/?api=1" +
                                                "&origin=o" +
                                                "&origin_place_id=${sourcePlaceId}" +
                                                "&destination=d" +
                                                "&destination_place_id=${destinationPlaceId}" +
                                                "&travelmode=transit"
                                    )
                                )
                            ),
                            backgroundColor = secondaryContainer,
                            contentColor = onSecondaryContainer,
                        )
                        Spacer(GlanceModifier.size(4.dp))
                        CircleIconButton(
                            ImageProvider(
                                R.drawable.baseline_refresh_24_white
                            ),
                            "Refresh",
                            onClick = {
                                actionRunCallback<RefreshAction>()
                            },
                            backgroundColor = DynamicThemeColorProviders.primary,
                            contentColor = DynamicThemeColorProviders.onPrimary,
                        )
                    }
                }
                Row(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier.fillMaxWidth().clickable(
                        actionStartActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://www.google.com/maps/dir/?api=1" +
                                            "&origin=o" +
                                            "&origin_place_id=${sourcePlaceId}" +
                                            "&destination=d" +
                                            "&destination_place_id=${destinationPlaceId}" +
                                            "&travelmode=transit"
                                )
                            )
                        )
                    )
                ) {
                    for ((k, bigStep: Step) in leg.steps.withIndex()) {
                        DisplayStep(bigStep)
                        if (k < leg.steps.lastIndex) {
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

    @Composable
    fun DisplayStep(bigStep: Step) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val travelModeText = getTravelModeText(bigStep)
            Row(verticalAlignment = Alignment.Bottom) {
                Image(
                    provider = ImageProvider(getTravelModeIconResource(travelModeText)),
//                            provider = ImageProvider(R.drawable.baseline_directions_transit_24_white),
                    contentDescription = travelModeText,
                    colorFilter = ColorFilter.tint(onBackground)
                )
                if (bigStep.transitDetails?.line != null) {
                    val text = if (bigStep.transitDetails.line.shortName.isNotBlank()) {
                        bigStep.transitDetails.line.shortName
                    } else if (bigStep.transitDetails.line.name.isNotBlank()) {
                        bigStep.transitDetails.line.name
                    } else {
                        return@Row
                    }
                    val textColor = if (bigStep.transitDetails.line.textColor.isNotBlank()) {
                        ColorProvider(Color(parseColor(bigStep.transitDetails.line.textColor)))
                    } else {
                        onSecondaryContainer
                    }
                    val backgroundTextColor = if (bigStep.transitDetails.line.color.isNotBlank()) {
                        ColorProvider(Color(parseColor(bigStep.transitDetails.line.color)))
                    } else {
                        secondaryContainer
                    }

                    Text(
                        text = text,
                        style = TextStyle(color = textColor),
                        modifier = GlanceModifier
                            .cornerRadius(4.dp)
                            .background(backgroundTextColor)
                            .padding(2.dp)
                    )
                }
            }
            Text(
                text = getTravelTime(bigStep),
                style = TextStyle(color = onBackground)
            )
        }
    }
}


class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("TransitWidget", "RefreshAction")
        WidgetSettingsRepo.update()
        TransitWidget().update(context, glanceId)
    }
}

class TransitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TransitWidget()

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        CoroutineScope(Dispatchers.IO).launch {
            WidgetSettingsRepo.update()
        }
    }

//    override fun onReceive(context: Context, intent: Intent) {
//        Log.d("TransitWidget", "Action broadcast: $intent")
//        intent.let {
//            if (it.action == AppWidgetManager.EXTRA_APPWIDGET_ID){
//                val source = it.getStringExtra(TransitWidgetExtension().APP_SETTINGS_SOURCE)
//                val destination = it.getStringExtra(TransitWidgetExtension().APP_SETTINGS_DESTINATION)
//                Log.d("TransitWidget","Source: $source")
//                Log.d("TransitWidget","Destination: $destination")
//                if (!source.isNullOrEmpty() && !destination.isNullOrEmpty())
//                {
//                    Log.d("TransitWidget", "Source and destination NOT null")
//                } else {
//                    Log.d("TransitWidget","Null values for source or destination")
//                }
////                runBlocking {
////                    val directionsResponse = ApiCaller.getDirectionsByName(source, destination)
////                    TransitWidget().updateData(context, AppSettings(lastDirectionsResponse = directionsResponse,
////                        source = Location(
////                            placeId = directionsResponse.geocodedWaypoints[0].placeId,
////                            name = source
////                        ),
////                        destination = Location(
////                            placeId = directionsResponse.geocodedWaypoints[1].placeId,
////                            name = destination
////                        )))
////                }
//            }
//
//
//        }
//        super.onReceive(context, intent)
//    }

//    override fun onUpdate(
//        context: Context,
//        appWidgetManager: AppWidgetManager,
//        appWidgetIds: IntArray
//    ) {
//        Log.d("TransitWidget", "onUpdate")
//
//        for (appWidgetId in appWidgetIds) {
//            intentAppWidget(context,appWidgetManager, appWidgetId)
//        }
////        actionRunCallback<RefreshAction>()
//        super.onUpdate(context, appWidgetManager, appWidgetIds)
//    }



//    private fun intentAppWidget(
//        context: Context,
//        appWidgetManager: AppWidgetManager,
//        appWidgetId: Int
//    ) {
//
//        val optionsBundle = appWidgetManager.getAppWidgetOptions(appWidgetId)
//        val source = optionsBundle.getString(TransitWidgetExtension().APP_SETTINGS_SOURCE)
//        val destination = optionsBundle.getString(TransitWidgetExtension().APP_SETTINGS_DESTINATION)
//        Log.d("TransitWidget","intentAppWidget")
//        if (source != null && destination != null) {
//            Log.d("TransitWidget","Source: $source")
//            Log.d("TransitWidget","Destination: $destination")
////            runBlocking {
////                val directionsResponse = ApiCaller.getDirectionsByName(source, destination)
////                TransitWidget().updateData(context, AppSettings(lastDirectionsResponse = directionsResponse,
////                    source = Location(
////                        placeId = directionsResponse.geocodedWaypoints[0].placeId,
////                        name = source
////                    ),
////                    destination = Location(
////                        placeId = directionsResponse.geocodedWaypoints[1].placeId,
////                        name = destination
////                    )))
////            }
//        } else {
//            Log.d("TransitWidget","Null values for source or destination")
//        }
//    }

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


