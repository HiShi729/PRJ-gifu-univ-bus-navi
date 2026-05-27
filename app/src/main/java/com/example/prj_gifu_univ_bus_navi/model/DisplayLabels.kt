package com.example.prj_gifu_univ_bus_navi.model

fun DestinationBusStop.displayName(): String = when (this) {
    DestinationBusStop.JR_GIFU -> "JR岐阜駅"
    DestinationBusStop.MEITETSU_GIFU -> "名鉄岐阜駅"
}
