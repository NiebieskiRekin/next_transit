package com.example.nexttransit.ui.widget

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.example.nexttransit.model.settings.AppSettings
import com.example.nexttransit.model.settings.AppSettingsSerializer
import java.io.File

class TransitWidgetStateDefinition : GlanceStateDefinition<AppSettings> {
    private val DATA_STORE_FILENAME_PREFIX = "widgetSettings_"

    override suspend fun getDataStore(context: Context, fileKey: String) = DataStoreFactory.create(
        serializer = AppSettingsSerializer,
        produceFile = { getLocation(context, fileKey) }
    )

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(DATA_STORE_FILENAME_PREFIX + fileKey.lowercase())
    }

}