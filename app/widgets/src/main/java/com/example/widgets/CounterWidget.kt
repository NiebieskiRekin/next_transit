package com.example.widgets
//
//import android.content.Context
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.sp
//import androidx.datastore.preferences.core.intPreferencesKey
//import androidx.glance.Button
//import androidx.glance.GlanceId
//import androidx.glance.GlanceModifier
//import androidx.glance.action.ActionParameters
//import androidx.glance.appwidget.GlanceAppWidget
//import androidx.glance.appwidget.GlanceAppWidgetReceiver
//import androidx.glance.appwidget.action.ActionCallback
//import androidx.glance.appwidget.action.actionRunCallback
//import androidx.glance.appwidget.provideContent
//import androidx.glance.appwidget.state.updateAppWidgetState
//import androidx.glance.background
//import androidx.glance.currentState
//import androidx.glance.layout.Alignment
//import androidx.glance.layout.Column
//import androidx.glance.layout.fillMaxSize
//import androidx.glance.state.GlanceStateDefinition
//import androidx.glance.state.PreferencesGlanceStateDefinition
//import androidx.glance.text.FontWeight
//import androidx.glance.text.Text
//import androidx.glance.text.TextStyle
//import androidx.glance.unit.ColorProvider
//
//
//private val countKey = intPreferencesKey("count")
//
//class CounterWidget : GlanceAppWidget() {
//
//    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
//
//    @Composable
//    fun Content() {
//        val count = currentState(key = countKey) ?: 0
//        Column(
//            modifier = GlanceModifier
//                .fillMaxSize()
//                .background(Color.DarkGray),
//            verticalAlignment = Alignment.Vertical.CenterVertically,
//            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
//        ){
//            Text(
//                text = count.toString(),
//                style = TextStyle(
//                    fontWeight = FontWeight.Medium,
//                    color = ColorProvider(Color.White),
//                    fontSize = 26.sp
//                )
//            )
//            Button(
//                text = "Inc",
//                onClick = actionRunCallback(IncrementActionCallback::class.java)
//            )
//        }
//    }
//
//    override suspend fun provideGlance(context: Context, id: GlanceId) {
//        provideContent {
//            Content()
//        }
//    }
//}


//class SimpleCounterWidgetReceiver : GlanceAppWidgetReceiver() {
//    override val glanceAppWidget: GlanceAppWidget = CounterWidget()
//}


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
//        CounterWidget().update(context, glanceId)
//    }
//
//}