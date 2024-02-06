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
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider


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
            Content()
        }
    }
}

class TransitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TransitWidget()
}