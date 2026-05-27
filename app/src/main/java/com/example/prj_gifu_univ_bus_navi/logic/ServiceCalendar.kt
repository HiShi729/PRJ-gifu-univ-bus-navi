package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.data.LocalHolidayData
import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import com.example.prj_gifu_univ_bus_navi.model.ServiceDateContext
import java.time.DayOfWeek
import java.time.LocalDate

object ServiceCalendar {
    fun createContext(date: LocalDate, schoolHolidays: List<LocalDate>): ServiceDateContext {
        val baseDayType = when {
            date in LocalHolidayData.holidays -> BaseDayType.WEEKEND_HOLIDAY
            date.dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> BaseDayType.WEEKEND_HOLIDAY
            else -> BaseDayType.WEEKDAY
        }
        return ServiceDateContext(
            date = date,
            baseDayType = baseDayType,
            isSchoolHoliday = date in schoolHolidays,
            month = date.monthValue,
        )
    }
}
