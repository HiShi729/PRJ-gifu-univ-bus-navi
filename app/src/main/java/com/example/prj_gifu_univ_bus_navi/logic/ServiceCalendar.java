package com.example.prj_gifu_univ_bus_navi.logic;

import com.example.prj_gifu_univ_bus_navi.data.LocalAcademicCalendarData;
import com.example.prj_gifu_univ_bus_navi.data.LocalHolidayData;
import com.example.prj_gifu_univ_bus_navi.model.BaseDayType;
import com.example.prj_gifu_univ_bus_navi.model.ServiceDateContext;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public final class ServiceCalendar {
    private ServiceCalendar() {
    }

    public static ServiceDateContext createContext(LocalDate date, List<LocalDate> schoolHolidays) {
        BaseDayType baseDayType;
        if (LocalHolidayData.isHoliday(date)
            || date.getDayOfWeek() == DayOfWeek.SATURDAY
            || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            baseDayType = BaseDayType.WEEKEND_HOLIDAY;
        } else {
            baseDayType = BaseDayType.WEEKDAY;
        }
        return new ServiceDateContext(
            date,
            baseDayType,
            LocalAcademicCalendarData.isSchoolHoliday(date),
            date.getMonthValue()
        );
    }
}
