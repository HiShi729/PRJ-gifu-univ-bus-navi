package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.data.LocalHolidayData
import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        val holiday = LocalHolidayData.getHolidays().first()

        assertEquals(BaseDayType.WEEKEND_HOLIDAY, ServiceCalendar.createContext(holiday, emptyList()).baseDayType)
    }

    @Test
    fun normalWeekdayUsesWeekdaySchedule() {
        assertEquals(BaseDayType.WEEKDAY, ServiceCalendar.createContext(LocalDate.of(2026, 5, 7), emptyList()).baseDayType)
    }

    @Test
    fun classPeriodWeekdayIsNotSchoolHoliday() {
        assertFalse(ServiceCalendar.createContext(LocalDate.of(2026, 4, 10), emptyList()).isSchoolHoliday)
    }

    @Test
    fun outsideClassPeriodWeekdayIsSchoolHoliday() {
        assertTrue(ServiceCalendar.createContext(LocalDate.of(2026, 8, 7), emptyList()).isSchoolHoliday)
    }

    @Test
    fun individualHolidayInClassPeriodIsSchoolHoliday() {
        assertTrue(ServiceCalendar.createContext(LocalDate.of(2026, 10, 30), emptyList()).isSchoolHoliday)
    }

    @Test
    fun winterBreakAndReturnDatesSetSchoolHolidayCorrectly() {
        assertTrue(ServiceCalendar.createContext(LocalDate.of(2027, 1, 4), emptyList()).isSchoolHoliday)
        assertFalse(ServiceCalendar.createContext(LocalDate.of(2027, 1, 5), emptyList()).isSchoolHoliday)
    }
}
