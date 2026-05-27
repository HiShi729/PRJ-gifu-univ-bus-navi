package com.example.prj_gifu_univ_bus_navi.data

import java.time.LocalDate

object LocalSchoolHolidayData {
    val schoolHolidays = buildList {
        addSchoolHolidayRange(LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31))
    }

    private fun MutableList<LocalDate>.addSchoolHolidayRange(start: LocalDate, endInclusive: LocalDate) {
        var current = start
        while (!current.isAfter(endInclusive)) {
            if (LocalAcademicCalendarData.isSchoolHoliday(current)) {
                add(current)
            }
            current = current.plusDays(1)
        }
    }
}
