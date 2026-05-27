package com.example.prj_gifu_univ_bus_navi.data

import com.example.prj_gifu_univ_bus_navi.model.BusStop
import com.example.prj_gifu_univ_bus_navi.model.BusStopId

object LocalBusStopData {
    val busStops = listOf(
        BusStop(BusStopId.GIFU_UNIV_HOSPITAL, "岐阜大学病院", "bus_stop_hospital"),
        BusStop(BusStopId.YANAGIDO, "柳戸橋", "bus_stop_yanagido"),
        BusStop(BusStopId.GIFU_UNIV, "岐阜大学", "bus_stop_university"),
    )
}
