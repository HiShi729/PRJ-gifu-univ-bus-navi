package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.data.LocalHolidayData
import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ServiceCalendarTest {
    @Test
    fun saturdayAndSundayUseWeekendHolidaySchedule() {
        assertEquals(BaseDayType.WEEKEND_HOLIDAY, ServiceCalendar.createContext(LocalDate.of(2026, 5, 2), emptyList()).baseDayType)
        assertEquals(BaseDayType.WEEKEND_HOLIDAY, ServiceCalendar.createContext(LocalDate.of(2026, 5, 3), emptyList()).baseDayType)
    }

    @Test
    fun localHolidayUsesWeekendHolidaySchedule() {
        val holiday = LocalHolidayData.holidays.first()

        assertEquals(BaseDayType.WEEKEND_HOLIDAY, ServiceCalendar.createContext(holiday, emptyList()).baseDayType)
    }

    @Test
    fun normalWeekdayUsesWeekdaySchedule() {
        assertEquals(BaseDayType.WEEKDAY, ServiceCalendar.createContext(LocalDate.of(2026, 5, 7), emptyList()).baseDayType)
    }
}
