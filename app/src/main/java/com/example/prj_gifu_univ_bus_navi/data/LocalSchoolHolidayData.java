package com.example.prj_gifu_univ_bus_navi.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LocalSchoolHolidayData {
    private static final List<LocalDate> SCHOOL_HOLIDAYS = Collections.unmodifiableList(buildSchoolHolidays());

    private LocalSchoolHolidayData() {
    }

    public static List<LocalDate> getSchoolHolidays() {
        return SCHOOL_HOLIDAYS;
    }

    private static List<LocalDate> buildSchoolHolidays() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = LocalDate.of(2026, 4, 1);
        LocalDate endInclusive = LocalDate.of(2027, 3, 31);
        while (!current.isAfter(endInclusive)) {
            if (LocalAcademicCalendarData.isSchoolHoliday(current)) {
                dates.add(current);
            }
            current = current.plusDays(1);
        }
        return dates;
    }
}
