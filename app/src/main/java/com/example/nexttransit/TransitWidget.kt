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
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight.Companion.Bold
import androidx.glance.text.Text
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
    fun Content(appSettings: AppSettings, context: Context){
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            DisplayDirectionsWidget(appSettings)
            Row(modifier = GlanceModifier.fillMaxWidth().padding(4.dp), horizontalAlignment = Alignment.Horizontal.End) {
                CircleIconButton(
                    imageProvider = ImageProvider(R.drawable.baseline_settings_24),
                    contentDescription = "Intent launch",
                    onClick = actionStartActivity(Intent(context,MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)),
                    backgroundColor = secondaryContainer,
                    contentColor = onSecondaryContainer,
                )
                Spacer(GlanceModifier.size(4.dp))
//            CircleIconButton(
//                ImageProvider(
//                    R.drawable.baseline_map_24
//                ),
//                "Open Google Maps",
//                onClick = actionStartActivity(
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        Uri.parse(
//                            "https://www.google.com/maps/dir/?api=1"+
//                                    "&origin=o"+
//                                    "&origin_place_id=ChIJLcfSImn7BEcRa3MR7sqwJsw"+
//                                    "&destination=d"+
//                                    "&destination_place_id=ChIJC0kwPxJbBEcRaulLN8Dqppc"+
//                                    "&travelmode=transit"
//                        )
//                    )
//                ),
//                backgroundColor = secondaryContainer,
//                contentColor = onSecondaryContainer,
//            )
//            Spacer(GlanceModifier.size(4.dp))
                CircleIconButton(
                    ImageProvider(
                        R.drawable.baseline_refresh_24_white
                    ),
                    "Refresh",
                    onClick = actionRunCallback<RefreshAction>(),
                    backgroundColor = primaryContainer,
                    contentColor = onPrimaryContainer,
                )
            }
        }
    }
    @Composable
    fun DisplayDirectionsWidget(appSettings: AppSettings) {
        val directions = appSettings.lastDirectionsResponse
        Log.e("GlanceWidget", directions.toString())
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(background)
                .padding(10.dp)
                .clickable(
                    actionStartActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                "https://www.google.com/maps/dir/?api=1" +
                                        "&origin=o" +
                                        "&origin_place_id=${appSettings.source.placeId}" +
                                        "&destination=d" +
                                        "&destination_place_id=${appSettings.destination.placeId}" +
                                        "&travelmode=transit"
                            )
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (directions.status) {
                "OK" -> {
                    if (directions.routes.isEmpty()) {
                        Text(
                            text = "Error: no route found.",
                            style = TextStyle(color = onBackground),
                        )
                    }
                    DisplayRoutes(routes = directions.routes)
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
    fun DisplayRoutes(routes: List<Route>) {
        for (route: Route in routes) {
            for (leg: Leg in route.legs) {
                Row(
                    horizontalAlignment = Alignment.Start,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(4.dp),
                ) {
                    Column (horizontalAlignment = Alignment.Horizontal.CenterHorizontally){
                        Text(
                            text = makeElipsis(leg.startAddress),
                            maxLines = 1,
                            style = TextStyle(
                                color = onBackground,
                                fontWeight = Bold,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = getLocalTime(leg.departureTime.value),
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
                            text = makeElipsis(leg.endAddress),
                            maxLines = 1,
                            style = TextStyle(
                                color = onBackground,
                                fontWeight = Bold,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = getLocalTime(leg.arrivalTime.value),
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
                        DisplayStep(bigStep)
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

    @Composable
    fun DisplayStep(bigStep: BigStep){
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
                        onPrimaryContainer
                    }
                    val backgroundTextColor =if (bigStep.transitDetails.line.color.isNotBlank()) {
                        ColorProvider(Color(parseColor(bigStep.transitDetails.line.color)))
                    } else  {
                        primaryContainer
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

private fun makeElipsis(string: String, count: Int = 20) : String {
    return string.take(count)+if(string.length <= count){""} else {"..."}
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


