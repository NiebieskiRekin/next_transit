//private val countKey = intPreferencesKey("count")
//
//class TransitWidget : GlanceAppWidget() {
//
//
//    @Composable
//    fun Content() {
//        val count = currentState(key = countKey) ?: 0

//            Button(
//                text = "Inc",
//                onClick = actionRunCallback(IncrementActionCallback::class.java)
//            )
//    }

//class IncrementActionCallback: ActionCallback {
//    override suspend fun onAction(
//        context: Context,
//        glanceId: GlanceId,
//        parameters: ActionParameters
//    ) {
//        updateAppWidgetState(context,glanceId) { prefs ->
//            val currentCount = prefs[countKey]
//            if (currentCount != null) {
//                prefs[countKey] = currentCount + 1
//            } else {
//                prefs[countKey] = 1
//            }
//        }
//        TransitWidget().update(context, glanceId)
//    }
//
//}



package com.example.nexttransit

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.DynamicThemeColorProviders.onSecondary
import androidx.glance.color.DynamicThemeColorProviders.secondary
import androidx.glance.layout.Alignment
import androidx.glance.layout.Alignment.Vertical.Companion.CenterVertically
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.FontWeight.Companion.Bold
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.nexttransit.ui.theme.NextTransitTheme


class TransitOptions {

}


class TransitWidget : GlanceAppWidget() {

    @Composable
    fun Content() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(color= Color.DarkGray)),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "A",
                        style = TextStyle(color = ColorProvider(color=Color.White))
                    )
                    Text(
                        text = " > ",
                        style = TextStyle(color = ColorProvider(color=Color.White))
                    )
                    Text(
                        text = "B",
                        style = TextStyle(color = ColorProvider(color=Color.White))
                    )
                    Spacer(GlanceModifier.size(16.dp))
                    Text(text = "16 min", style = TextStyle(color= ColorProvider(color=Color.White)))
                }
                Text(
                    text = "Departure: 21:37",
                    style = TextStyle(
                        color = ColorProvider(color=Color.White),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            SimpleDisplay()
        }
    }


    @Composable
    fun SimpleDisplay(directions: DirectionsResponse = ApiCaller.getSampleDirections()){
            when (directions.status){
                "OK" -> {
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .cornerRadius(20.dp)
                            .background(secondary)
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (directions.routes.isEmpty()) {

                                Text(
                                    text = "Error: no route found.",
                                    style = TextStyle(color=onSecondary),
                                )

                        }

                        for (route : Route in directions.routes) {
                            for (leg: Leg in route.legs) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Departure: ",
                                        style = TextStyle(
                                            color = onSecondary,
                                            fontWeight = Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = leg.departure_time.text,
                                        style = TextStyle(
                                            color = onSecondary,
                                            fontWeight = Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                    Spacer(GlanceModifier.size(16.dp))
                                    Text(
                                        text = "Planned Arrival: ",
                                        style = TextStyle(
                                            color = onSecondary,
                                            fontWeight = Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = leg.arrival_time.text,
                                        style = TextStyle(
                                            color = onSecondary,
                                            fontWeight = Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                                Row(verticalAlignment = CenterVertically) {
                                    for ((i, bigStep: BigStep) in leg.steps.withIndex()) {

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val travelModeText = getTravelModeText(bigStep)
                                                Text(
                                                    text = travelModeText,
                                                    style = TextStyle(color = onSecondary)
                                                )
//                                                Image(
//                                                    provider = ImageProvider(R.drawable.ic_launcher_foreground), // TODO()
//                                                    contentDescription = null,
//                                                    colorFilter = ColorFilter.tint(onSecondary)
//                                                )
                                                Text(
                                                    text = getTravelTime(bigStep),
                                                    style = TextStyle(color = onSecondary)
                                                )
                                            }
                                            if (i < leg.steps.lastIndex) {
                                                Text(
                                                    text = " > ",
                                                    style = TextStyle(color = onSecondary)
                                                )
                                            }

                                    }
                                }
                            }
                        }
                    }
                }

                "Error" -> Text(text="Error: directions data not available.")
                "Empty" -> Text(text="")
                else -> {}
            }
        }

}

class TransitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TransitWidget()
}

