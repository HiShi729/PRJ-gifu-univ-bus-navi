package com.example.prj_gifu_univ_bus_navi.data

import java.time.LocalDate

object LocalSchoolHolidayData {
    // TODO: 岐阜大学公式カレンダーで確定日を確認し、必要に応じて更新する。
    val schoolHolidays = buildList {
        addRange(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 7))
        addRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 30))
        addRange(LocalDate.of(2026, 12, 24), LocalDate.of(2027, 1, 7))
        addRange(LocalDate.of(2027, 2, 12), LocalDate.of(2027, 3, 31))
        add(LocalDate.of(2026, 11, 20))
    }

    private fun MutableList<LocalDate>.addRange(start: LocalDate, endInclusive: LocalDate) {
        var current = start
        while (!current.isAfter(endInclusive)) {
            add(current)
            current = current.plusDays(1)
        }
    }
}
