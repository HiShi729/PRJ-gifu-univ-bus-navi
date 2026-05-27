package com.example.prj_gifu_univ_bus_navi.data

import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.OperationRule
import java.time.LocalTime

object LocalBusScheduleData {
    val busTrips = listOf(
        BusTrip("wd-1800", "JR岐阜駅・名鉄岐阜駅", BaseDayType.WEEKDAY, "清流ライナー", LocalTime.of(17, 58), LocalTime.of(18, 2), LocalTime.of(18, 5), LocalTime.of(18, 33), LocalTime.of(18, 36), OperationRule.NONE, null, null, true),
        BusTrip("wd-1808", "JR岐阜駅", BaseDayType.WEEKDAY, "岐阜大学線", LocalTime.of(18, 4), LocalTime.of(18, 8), LocalTime.of(18, 11), LocalTime.of(18, 38), null, OperationRule.NONE, null, null, false),
        BusTrip("wd-1815", "名鉄岐阜駅", BaseDayType.WEEKDAY, "岐阜大学線", LocalTime.of(18, 11), LocalTime.of(18, 15), LocalTime.of(18, 18), null, LocalTime.of(18, 47), OperationRule.SCHOOL_HOLIDAY_EXCLUDED, null, null, false),
        BusTrip("wd-holiday-1825", "JR岐阜駅・名鉄岐阜駅", BaseDayType.WEEKDAY, "学休日臨時", LocalTime.of(18, 21), LocalTime.of(18, 25), LocalTime.of(18, 28), LocalTime.of(18, 55), LocalTime.of(18, 58), OperationRule.SCHOOL_HOLIDAY_ONLY, null, null, false),
        BusTrip("wd-limited-1835", "JR岐阜駅", BaseDayType.WEEKDAY, "春学期増便", LocalTime.of(18, 31), LocalTime.of(18, 35), LocalTime.of(18, 38), LocalTime.of(19, 6), null, OperationRule.LIMITED_PERIOD, 4, 8, false),
        BusTrip("wd-limited-school-1845", "JR岐阜駅・名鉄岐阜駅", BaseDayType.WEEKDAY, "講義日増便", LocalTime.of(18, 41), LocalTime.of(18, 45), LocalTime.of(18, 48), LocalTime.of(19, 16), LocalTime.of(19, 19), OperationRule.LIMITED_PERIOD_AND_SCHOOL_HOLIDAY_EXCLUDED, 4, 8, false),
        BusTrip("we-1000", "JR岐阜駅・名鉄岐阜駅", BaseDayType.WEEKEND_HOLIDAY, "休日岐阜大学線", LocalTime.of(10, 0), LocalTime.of(10, 4), LocalTime.of(10, 7), LocalTime.of(10, 35), LocalTime.of(10, 38), OperationRule.NONE, null, null, false),
        BusTrip("we-1030", "JR岐阜駅", BaseDayType.WEEKEND_HOLIDAY, "休日岐阜大学線", LocalTime.of(10, 30), LocalTime.of(10, 34), LocalTime.of(10, 37), LocalTime.of(11, 5), null, OperationRule.NONE, null, null, false),
    )
}
