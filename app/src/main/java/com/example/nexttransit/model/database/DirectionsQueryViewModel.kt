package com.example.nexttransit.model.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexttransit.model.calendar.Event
import com.example.nexttransit.model.routes.DirectionsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DirectionsQueryViewModel(
    private val dao: DirectionsQueryDao
):ViewModel(){
    private val _state = MutableStateFlow(DirectionsState())
    private val _directions = dao.getAllDirectionsQueries().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
//    private val _selectedDirections = dao.getDirectionsQuery(1).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val state: StateFlow<DirectionsState> = combine(_state,_directions) { state, directions ->
            state.copy(
                directions = directions
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DirectionsState())

    fun onEvent(event: DirectionsEvent){
        when(event){
            DirectionsEvent.SaveDirectionsQuery -> {
                if (_state.value.firstEvent == Event() || _state.value.secondEvent == Event() || _state.value.directionsResponse == DirectionsResponse()){
                    return
                }

                viewModelScope.launch {
                    dao.upsertDirectionsQuery(
                        DirectionsQuery(
                            firstEvent = _state.value.firstEvent,
                            secondEvent = _state.value.secondEvent,
                            directionsResponse = _state.value.directionsResponse
                        )
                    )
                }

                _state.update { it.copy(
                    firstEvent = Event(),
                    secondEvent = Event(),
                    directionsResponse = DirectionsResponse()
                ) }
            }
            is DirectionsEvent.SetDirectionsResponse -> {
                _state.update {
                    it.copy(
                        directionsResponse = event.directionsResponse
                    )
                }
            }
            is DirectionsEvent.SetFirstEvent -> {
                _state.update {
                    it.copy(
                        firstEvent = event.firstEvent
                    )
                }
            }
            is DirectionsEvent.SetSecondEvent -> {
                _state.update {
                    it.copy(
                        secondEvent = event.secondEvent
                    )
                }
            }
        }
    }
}