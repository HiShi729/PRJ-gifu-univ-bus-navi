package com.example.prj_gifu_univ_bus_navi.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LocalAcademicCalendarDataTest {
    @Test
    fun classPeriodBoundaryWeekdaysAreClassDays() {
        assertTrue(LocalAcademicCalendarData.isClassDay(LocalDate.of(2026, 4, 10)))
        assertTrue(LocalAcademicCalendarData.isClassDay(LocalDate.of(2026, 8, 6)))
        assertTrue(LocalAcademicCalendarData.isClassDay(LocalDate.of(2026, 10, 1)))
        assertTrue(LocalAcademicCalendarData.isClassDay(LocalDate.of(2027, 2, 5)))
    }

    @Test
    fun datesOutsideClassPeriodsAreSchoolHolidays() {
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2026, 8, 7)))
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2027, 2, 6)))
    }

    @Test
    fun individualHolidaysAreSchoolHolidays() {
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2026, 10, 30)))
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2026, 12, 26)))
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2027, 1, 4)))
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2027, 1, 15)))
    }

    @Test
    fun weekdaysAfterWinterBreakReturnToClassDays() {
        assertFalse(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2026, 12, 25)))
        assertFalse(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2027, 1, 5)))
    }

    @Test
    fun weekendAndHolidayInsideClassPeriodAreSchoolHolidays() {
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2026, 4, 11)))
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2026, 4, 12)))
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2026, 7, 20)))
    }
}
