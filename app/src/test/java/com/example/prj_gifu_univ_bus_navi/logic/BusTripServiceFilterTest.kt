package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.OperationRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class BusTripServiceFilterTest {
    @Test
    fun schoolHolidayExcludedTripIsExcludedOnSchoolHoliday() {
        val trip = trip(OperationRule.SCHOOL_HOLIDAY_EXCLUDED)
        val context = ServiceCalendar.createContext(LocalDate.of(2026, 5, 1), listOf(LocalDate.of(2026, 5, 1)))

        assertFalse(BusTripServiceFilter.isTripAvailable(trip, context))
    }

    @Test
    fun schoolHolidayOnlyTripIsAvailableOnlyOnSchoolHoliday() {
        val trip = trip(OperationRule.SCHOOL_HOLIDAY_ONLY)
        val holiday = ServiceCalendar.createContext(LocalDate.of(2026, 5, 1), listOf(LocalDate.of(2026, 5, 1)))
        val normalDay = ServiceCalendar.createContext(LocalDate.of(2026, 5, 4), emptyList())

        assertTrue(BusTripServiceFilter.isTripAvailable(trip, holiday))
        assertFalse(BusTripServiceFilter.isTripAvailable(trip, normalDay))
    }

    @Test
    fun limitedPeriodTripIsAvailableInMayAndExcludedInSeptember() {
        val trip = trip(OperationRule.LIMITED_PERIOD, 4, 8)

        assertTrue(BusTripServiceFilter.isTripAvailable(trip, ServiceCalendar.createContext(LocalDate.of(2026, 5, 4), emptyList())))
        assertFalse(BusTripServiceFilter.isTripAvailable(trip, ServiceCalendar.createContext(LocalDate.of(2026, 9, 1), emptyList())))
    }

    @Test
    fun limitedPeriodAndSchoolHolidayExcludedTripChecksBothConditions() {
        val trip = trip(OperationRule.LIMITED_PERIOD_AND_SCHOOL_HOLIDAY_EXCLUDED, 4, 8)
        val normalDay = ServiceCalendar.createContext(LocalDate.of(2026, 5, 4), emptyList())
        val schoolHoliday = ServiceCalendar.createContext(LocalDate.of(2026, 5, 1), listOf(LocalDate.of(2026, 5, 1)))

        assertTrue(BusTripServiceFilter.isTripAvailable(trip, normalDay))
        assertFalse(BusTripServiceFilter.isTripAvailable(trip, schoolHoliday))
    }

    private fun trip(rule: OperationRule, startMonth: Int? = null, endMonth: Int? = null) = BusTrip(
        id = "test",
        destination = "JR岐阜駅",
        baseDayType = BaseDayType.WEEKDAY,
        routeName = "テスト",
        hospitalDepartureTime = LocalTime.of(18, 0),
        yanagidoDepartureTime = LocalTime.of(18, 4),
        universityDepartureTime = LocalTime.of(18, 7),
        jrGifuArrivalTime = LocalTime.of(18, 35),
        meitetsuGifuArrivalTime = null,
        operationRule = rule,
        operatingStartMonth = startMonth,
        operatingEndMonth = endMonth,
        mayBeArticulatedBus = false,
    )
}
