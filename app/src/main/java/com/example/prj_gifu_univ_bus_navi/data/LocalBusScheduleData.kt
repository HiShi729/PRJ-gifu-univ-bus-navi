package com.example.prj_gifu_univ_bus_navi.data

import android.content.Context
import com.example.prj_gifu_univ_bus_navi.model.BusTrip

object LocalBusScheduleData {
    fun loadBusTrips(context: Context): List<BusTrip> =
        context.assets.open("bus_schedule.csv").bufferedReader().use { reader ->
            BusScheduleCsvParser.parse(reader.readText())
        }
}
