package com.example.nexttransit

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object WidgetSettingsRepo {
    private var _currentSettings = MutableStateFlow(AppSettings())
    val currentSettings: StateFlow<AppSettings> get() = _currentSettings

    suspend fun updateByName(source: String, destination: String) {

        val directionsResponse = ApiCaller.getDirectionsByName(source, destination)
        _currentSettings.value = AppSettings(
            source = Location(
                placeId = directionsResponse.geocodedWaypoints[0].placeId,
                name = source
            ),
            destination = Location(
                placeId = directionsResponse.geocodedWaypoints[1].placeId,
                name = destination
            ),
            directionsResponse
        )
    }

    suspend fun updateByPlaceId(source: PlaceId, destination: PlaceId) {
        val directionsResponse =
            ApiCaller.getDirectionsByPlaceId(source, destination)


        _currentSettings.value = AppSettings(
            source = Location(
                placeId = source,
                name = if (_currentSettings.value.source.name.isBlank()) {
                    directionsResponse.routes[0].legs[0].startAddress
                } else _currentSettings.value.source.name
            ),
            destination = Location(
                placeId = destination,
                name = if (_currentSettings.value.destination.name.isBlank()) {
                    directionsResponse.routes[0].legs[0].endAddress
                } else _currentSettings.value.destination.name
            ),
            directionsResponse
        )
    }

    suspend fun update(){

        val source = _currentSettings.value.source
        val destination = _currentSettings.value.destination

        _currentSettings.value = AppSettings(
            source = source,
            destination = destination,
            ApiCaller.getDirectionsByPlaceId(source.placeId, destination.placeId)
        )
    }


}
