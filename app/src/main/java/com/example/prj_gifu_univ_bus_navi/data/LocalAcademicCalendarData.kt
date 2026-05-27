package com.example.prj_gifu_univ_bus_navi.data

import java.time.DayOfWeek
import java.time.LocalDate

object LocalAcademicCalendarData {
    private val classPeriods = listOf(
        DateRange(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 8, 6)),
        DateRange(LocalDate.of(2026, 10, 1), LocalDate.of(2027, 2, 5)),
    )

    private val individualHolidays = buildSet {
        add(LocalDate.of(2026, 10, 30))
        addRange(LocalDate.of(2026, 12, 26), LocalDate.of(2027, 1, 4))
        add(LocalDate.of(2027, 1, 15))
    }

    fun isClassDay(date: LocalDate): Boolean =
        classPeriods.any { it.contains(date) } &&
            date.dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) &&
            date !in LocalHolidayData.holidays &&
            date !in individualHolidays

    fun isSchoolHoliday(date: LocalDate): Boolean = !isClassDay(date)

    private data class DateRange(
        val start: LocalDate,
        val endInclusive: LocalDate,
    ) {
        fun contains(date: LocalDate): Boolean =
            !date.isBefore(start) && !date.isAfter(endInclusive)
    }

    private fun MutableSet<LocalDate>.addRange(start: LocalDate, endInclusive: LocalDate) {
        var current = start
        while (!current.isAfter(endInclusive)) {
            add(current)
            current = current.plusDays(1)
        }
    }
}
