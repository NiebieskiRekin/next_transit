package com.example.nexttransit

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color.parseColor
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight.Companion.Bold
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.nexttransit.MainActivity.Companion.appSettingsDataStore
import kotlinx.coroutines.runBlocking


class TransitWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
//        val openMainActivity = Intent(context,MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        provideContent {
//            val addPendingIntent = PendingIntent.getBroadcast(context, 0, openMainActivity, 0 or PendingIntent.FLAG_MUTABLE)
            val appSettings = context.appSettingsDataStore.data.collectAsState(initial = AppSettings()).value
            Content(appSettings,context)
        }
    }

//    fun onNewIntent(intent: Intent?) {
//        val source: String? = intent?.getStringExtra("source")
//        val destination: String? = intent?.getStringExtra("destination")
//        val directions: DirectionsResponse? =
//            intent?.getStringExtra("directions")?.let {
//                Json.decodeFromString(
//                    deserializer = DirectionsResponse.serializer(),
//                    string = it
//                )
//            }
//    }
    @Composable
    fun Content(appSettings: AppSettings,context: Context) {
        val directions = appSettings.lastDirectionsResponse
        Log.e("GlanceWidget", directions.toString())
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
                    DisplayRoutes(routes = directions.routes,
                        appSettings.source.placeId,appSettings.destination.placeId,context)
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
    fun DisplayRoutes(routes: List<Route>, sourcePlaceId: PlaceId, destinationPlaceId: PlaceId,context: Context) {
        routes.forEachIndexed { i: Int,route: Route ->
            route.legs.forEachIndexed { j: Int,leg: Leg ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.Top
                ) {
                    Row(modifier=GlanceModifier
                        .defaultWeight()
                        .clickable(actionStartActivity(
                        Intent(context,MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        )) {
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
                        if (i==0 && j==0){
                            CircleIconButton(
                                ImageProvider(
                                    R.drawable.baseline_map_24
                                ),
                                "Open Google Maps",
                                onClick =actionStartActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            "https://www.google.com/maps/dir/?api=1" +
                                                    "&origin=o" +
                                                    "&origin_place_id=${sourcePlaceId }" +
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
                                onClick = actionRunCallback<RefreshAction>(),
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
    fun DisplayStep(bigStep: Step){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val travelModeText = getTravelModeText(bigStep)
            Row(verticalAlignment = Alignment.Bottom){
                Image(
                    provider = ImageProvider(getTravelModeIconResource(travelModeText)),
//                            provider = ImageProvider(R.drawable.baseline_directions_transit_24_white),
                    contentDescription = travelModeText,
                    colorFilter = ColorFilter.tint(onBackground)
                )
                if (bigStep.transitDetails?.line != null) {
                    val text = if (bigStep.transitDetails.line.shortName.isNotBlank()){
                        bigStep.transitDetails.line.shortName
                    } else if (bigStep.transitDetails.line.name.isNotBlank()){
                        bigStep.transitDetails.line.name
                    } else {
                        return@Row
                    }
                    val textColor = if (bigStep.transitDetails.line.textColor.isNotBlank()) {
                        ColorProvider(Color(parseColor(bigStep.transitDetails.line.textColor)))
                    } else {
                        onSecondaryContainer
                    }
                    val backgroundTextColor =if (bigStep.transitDetails.line.color.isNotBlank()) {
                        ColorProvider(Color(parseColor(bigStep.transitDetails.line.color)))
                    } else  {
                        secondaryContainer
                    }

                    Text(
                        text=text,
                        style=TextStyle(color=textColor),
                        modifier=GlanceModifier
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
        TransitWidget().update(context, glanceId)
        runBlocking{
            context.appSettingsDataStore.updateData {
                it.copy(
                    lastDirectionsResponse=ApiCaller.getDirectionsByPlaceId(
                        it.source.placeId,it.destination.placeId)
                )
            }}
    }
}

class TransitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TransitWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        runBlocking{
        context.appSettingsDataStore.updateData {
            it.copy(
                lastDirectionsResponse=ApiCaller.getDirectionsByPlaceId(
                    it.source.placeId,it.destination.placeId)
            )
        }}
    }
}

//private fun getTravelModeEmoji(travelMode: String) = when (travelMode) {
//    "TRANSIT" -> "\uD83D\uDE80"
//    "WALKING" -> "\uD83D\uDEB6"
//    "BICYCLING" -> "\uD83D\uDEB2"
//    "DRIVING" -> "\uD83D\uDE97"
//    "BUS" -> "\uD83D\uDE8C"
//    "TRAM" -> "\uD83D\uDE8B"
//    "HEAVY_RAIL" -> "\uD83D\uDE82"
//    "BOAT" -> "⛵"
//    else -> "?"
//}

//private fun makeEllipsis(string: String, count: Int = 20) : String {
//    return string.take(count)+if(string.length <= count){""} else {"..."}
//}

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


