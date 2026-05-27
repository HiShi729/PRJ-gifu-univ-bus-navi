package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.DestinationBusStop
import java.time.LocalTime

object DestinationStopMatcher {
    fun resolveArrival(
        trip: BusTrip,
        selectedDestination: DestinationBusStop,
    ): Pair<DestinationBusStop, LocalTime>? = when (selectedDestination) {
        DestinationBusStop.JR_GIFU -> trip.jrGifuArrivalTime?.let { DestinationBusStop.JR_GIFU to it }
        DestinationBusStop.MEITETSU_GIFU -> when {
            trip.meitetsuGifuArrivalTime != null -> DestinationBusStop.MEITETSU_GIFU to trip.meitetsuGifuArrivalTime
            trip.jrGifuArrivalTime != null -> DestinationBusStop.JR_GIFU to trip.jrGifuArrivalTime
            else -> null
        }
    }
}
