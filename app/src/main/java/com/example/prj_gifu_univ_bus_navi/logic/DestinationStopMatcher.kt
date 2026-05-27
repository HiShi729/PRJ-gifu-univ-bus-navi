package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import java.time.LocalTime

object DestinationStopMatcher {
    fun resolveArrival(
        trip: BusTrip,
        selectedDestinationStopName: String,
    ): Pair<String, LocalTime>? = when (selectedDestinationStopName) {
        "JR岐阜" -> trip.stopTime("JR岐阜")?.let { "JR岐阜" to it }
        "名鉄岐阜" -> when {
            trip.stopTime("名鉄岐阜") != null -> "名鉄岐阜" to trip.stopTime("名鉄岐阜")!!
            trip.stopTime("JR岐阜") != null -> "JR岐阜" to trip.stopTime("JR岐阜")!!
            else -> null
        }
        else -> trip.stopTime(selectedDestinationStopName)?.let { selectedDestinationStopName to it }
    }

    private fun BusTrip.stopTime(stopName: String): LocalTime? =
        stopTimes[stopName] ?: when (stopName) {
            "JR岐阜" -> jrGifuArrivalTime
            "名鉄岐阜" -> meitetsuGifuArrivalTime
            else -> null
        }
}
