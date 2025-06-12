package com.example.nexttransit.ui.widget

import com.example.nexttransit.model.routes.Leg
import com.example.nexttransit.model.routes.Route
import com.example.nexttransit.model.routes.Step
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalGlanceId
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
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
import androidx.glance.text.FontWeight.Companion.Bold
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.nexttransit.MainActivity
import com.example.nexttransit.R
import com.example.nexttransit.ServiceLocator
import com.example.nexttransit.api.ApiCaller
import com.example.nexttransit.getLocalTime
import com.example.nexttransit.getTravelModeIconResource
import com.example.nexttransit.getTravelModeText
import com.example.nexttransit.getTravelTime
import com.example.nexttransit.model.settings.AppSettings
import com.example.nexttransit.model.routes.DirectionsResponse
import com.example.nexttransit.model.routes.PlaceId
import kotlinx.coroutines.coroutineScope

class TransitWidget : GlanceAppWidget() {

    companion object {
        private val smallMode = DpSize(250.dp, 60.dp)
        private val largeMode = DpSize(250.dp, 120.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(smallMode, largeMode)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val db = ServiceLocator.provideDatabase(context)
            val api = ServiceLocator.provideApiService()
            val firestore = ServiceLocator.provideFirestore()
            val appSettings = ServiceLocator.provideDataStore(context)

            val settings = appSettings.data.collectAsState(initial = AppSettings())
            Log.d("GlanceWidget", LocalGlanceId.current.toString())
            Column {
                Content(
                    settings.value.lastDirectionsResponse,
                    settings.value.source.placeId,
                    settings.value.destination.placeId,
                    context
                )
                if (LocalSize.current == largeMode) {
                    Content(
                        directions = settings.value.returnResponse,
                        sourcePlaceId = settings.value.secondSource.placeId,
                        destinationPlaceId = settings.value.secondDestination.placeId,
                        context = context
                    )
                }
//                Text(
//                    if (LocalGlanceId.current.toInt() % 2 == 0){
//                        "even ${LocalGlanceId.current.toInt()}"
//                    } else {
//                        "odd ${LocalGlanceId.current.toInt()}"
//                    }, GlanceModifier.background(background).fillMaxSize(),
//                    TextStyle(color=onBackground)
//                )
            }

        }
    }

    @Composable
    fun Content(
        directions: DirectionsResponse,
        sourcePlaceId: PlaceId,
        destinationPlaceId: PlaceId,
        context: Context
    ) {
        Log.e("GlanceWidget", directions.toString())
        Column(
            modifier = GlanceModifier
                .cornerRadius(20.dp)
                .background(background)
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.Top,
        ) {
            when (directions.status) {
                "OK" -> {
                    if (directions.routes.isEmpty()) {
                        ShowError(text = "No route found.")
                    } else {
                        DisplayRoutes(
                            routes = directions.routes,
                            sourcePlaceId,
                            destinationPlaceId,
                            context
                        )
                    }
                }

                "Error" -> ShowError(text = "Directions data not available.")
                else -> ShowError(text = "Empty Response.")
            }
        }
    }

    @Composable
    fun ShowError(text: String) {
        Row(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Column(GlanceModifier.defaultWeight()) {
                Text(
                    text = "Error: ",
                    style = TextStyle(color = onBackground, fontWeight = Bold),
                    modifier = GlanceModifier
                )
                Text(
                    text = text,
                    style = TextStyle(color = onBackground),
                    modifier = GlanceModifier
                )
            }
            RefreshButton()
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
                        OpenGoogleMapsButton(
                            sourcePlaceId = sourcePlaceId,
                            destinationPlaceId = destinationPlaceId
                        )
                        Spacer(GlanceModifier.size(4.dp))
                        RefreshButton()
                    }
                }
                Row(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier.fillMaxWidth().clickable(
                        actionStartActivity(
                            googleMapsIntent(sourcePlaceId, destinationPlaceId)
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
    fun OpenGoogleMapsButton(sourcePlaceId: PlaceId, destinationPlaceId: PlaceId) {
        CircleIconButton(
            ImageProvider(
                R.drawable.baseline_map_24
            ),
            "Open Google Maps",
            onClick = actionStartActivity(
                googleMapsIntent(sourcePlaceId, destinationPlaceId)
            ),
            backgroundColor = secondaryContainer,
            contentColor = onSecondaryContainer,
        )
    }

    private fun googleMapsIntent(sourcePlaceId: PlaceId, destinationPlaceId: PlaceId) =
        Intent(
            Intent.ACTION_VIEW,
            ("https://www.google.com/maps/dir/?api=1" +
                    "&origin=o" +
                    "&origin_place_id=${sourcePlaceId}" +
                    "&destination=d" +
                    "&destination_place_id=${destinationPlaceId}" +
                    "&travelmode=transit").toUri()
        )


    @Composable
    fun RefreshButton(gl: GlanceModifier = GlanceModifier) {
        CircleIconButton(
            ImageProvider(
                R.drawable.baseline_refresh_24_white
            ),
            "Refresh",
            onClick = actionRunCallback<RefreshAction>(),
            backgroundColor = DynamicThemeColorProviders.primary,
            contentColor = DynamicThemeColorProviders.onPrimary,
            modifier = gl
        )
    }

    @SuppressLint("RestrictedApi")
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
                        ColorProvider(Color(bigStep.transitDetails.line.textColor.toColorInt()))
                    } else {
                        onSecondaryContainer
                    }
                    val backgroundTextColor = if (bigStep.transitDetails.line.color.isNotBlank()) {
                        ColorProvider(Color(bigStep.transitDetails.line.color.toColorInt()))
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
        Log.d("TransitWidget", glanceId.toString())
        val appSettings = ServiceLocator.provideDataStore(context)

        coroutineScope {
            appSettings.updateData {
                it.copy(
                    lastDirectionsResponse = ApiCaller.getDirectionsByPlaceId(
                        it.source.placeId, it.destination.placeId
                    )
                )
            }
        }
        TransitWidget().update(context, glanceId)
    }
}

class TransitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TransitWidget()
}




