package com.example.prj_gifu_univ_bus_navi.data;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class LocalHolidayData {
    private static final Set<LocalDate> HOLIDAYS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 12),
        LocalDate.of(2026, 2, 11),
        LocalDate.of(2026, 2, 23),
        LocalDate.of(2026, 3, 20),
        LocalDate.of(2026, 4, 29),
        LocalDate.of(2026, 5, 3),
        LocalDate.of(2026, 5, 4),
        LocalDate.of(2026, 5, 5),
        LocalDate.of(2026, 5, 6),
        LocalDate.of(2026, 7, 20),
        LocalDate.of(2026, 8, 11),
        LocalDate.of(2026, 9, 21),
        LocalDate.of(2026, 9, 22),
        LocalDate.of(2026, 9, 23),
        LocalDate.of(2026, 10, 12),
        LocalDate.of(2026, 11, 3),
        LocalDate.of(2026, 11, 23)
    )));

    private LocalHolidayData() {
    }

    public static boolean isHoliday(LocalDate date) {
        return HOLIDAYS.contains(date);
    }

    public static Set<LocalDate> getHolidays() {
        return HOLIDAYS;
    }
}
