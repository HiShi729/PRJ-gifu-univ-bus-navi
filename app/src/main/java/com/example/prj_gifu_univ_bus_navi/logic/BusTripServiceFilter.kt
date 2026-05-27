package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.OperationRule
import com.example.prj_gifu_univ_bus_navi.model.ServiceDateContext

object BusTripServiceFilter {
    fun isTripAvailable(trip: BusTrip, context: ServiceDateContext): Boolean {
        if (trip.baseDayType != context.baseDayType) return false
        return when (trip.operationRule) {
            OperationRule.NONE -> true
            OperationRule.SCHOOL_HOLIDAY_EXCLUDED -> !context.isSchoolHoliday
            OperationRule.SCHOOL_HOLIDAY_ONLY -> context.isSchoolHoliday
            OperationRule.LIMITED_PERIOD -> isInOperatingMonth(trip, context.month)
            OperationRule.LIMITED_PERIOD_AND_SCHOOL_HOLIDAY_EXCLUDED -> {
                isInOperatingMonth(trip, context.month) && !context.isSchoolHoliday
            }
        }
    }

    fun filterAvailableTrips(trips: List<BusTrip>, context: ServiceDateContext): List<BusTrip> =
        trips.filter { isTripAvailable(it, context) }

    private fun isInOperatingMonth(trip: BusTrip, month: Int): Boolean {
        val start = trip.operatingStartMonth ?: return false
        val end = trip.operatingEndMonth ?: return false
        return if (start <= end) {
            month in start..end
        } else {
            month >= start || month <= end
        }
    }
}
