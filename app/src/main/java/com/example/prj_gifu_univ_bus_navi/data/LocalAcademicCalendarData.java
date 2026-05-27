package com.example.prj_gifu_univ_bus_navi.data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class LocalAcademicCalendarData {
    private static final List<DateRange> CLASS_PERIODS = Collections.unmodifiableList(Arrays.asList(
        new DateRange(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 8, 6)),
        new DateRange(LocalDate.of(2026, 10, 1), LocalDate.of(2027, 2, 5))
    ));
    private static final List<LocalDate> INDIVIDUAL_HOLIDAYS = Collections.unmodifiableList(buildIndividualHolidays());

    private LocalAcademicCalendarData() {
    }

    public static boolean isClassDay(LocalDate date) {
        return isInClassPeriod(date)
            && date.getDayOfWeek() != DayOfWeek.SATURDAY
            && date.getDayOfWeek() != DayOfWeek.SUNDAY
            && !LocalHolidayData.isHoliday(date)
            && !INDIVIDUAL_HOLIDAYS.contains(date);
    }

    public static boolean isSchoolHoliday(LocalDate date) {
        return !isClassDay(date);
    }

    private static boolean isInClassPeriod(LocalDate date) {
        for (DateRange period : CLASS_PERIODS) {
            if (period.contains(date)) {
                return true;
            }
        }
        return false;
    }

    private static List<LocalDate> buildIndividualHolidays() {
        List<LocalDate> holidays = new ArrayList<>();
        holidays.add(LocalDate.of(2026, 10, 30));
        addRange(holidays, LocalDate.of(2026, 12, 26), LocalDate.of(2027, 1, 4));
        holidays.add(LocalDate.of(2027, 1, 15));
        return holidays;
    }

    private static void addRange(List<LocalDate> dates, LocalDate start, LocalDate endInclusive) {
        LocalDate current = start;
        while (!current.isAfter(endInclusive)) {
            dates.add(current);
            current = current.plusDays(1);
        }
    }

    private static final class DateRange {
        private final LocalDate start;
        private final LocalDate endInclusive;

        private DateRange(LocalDate start, LocalDate endInclusive) {
            this.start = start;
            this.endInclusive = endInclusive;
        }

        private boolean contains(LocalDate date) {
            return !date.isBefore(start) && !date.isAfter(endInclusive);
        }
    }
}
