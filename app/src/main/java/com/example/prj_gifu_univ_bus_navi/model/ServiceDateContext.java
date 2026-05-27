package com.example.prj_gifu_univ_bus_navi.model;

import java.time.LocalDate;
import java.util.Objects;

public final class ServiceDateContext {
    private final LocalDate date;
    private final BaseDayType baseDayType;
    private final boolean schoolHoliday;
    private final int month;

    public ServiceDateContext(LocalDate date, BaseDayType baseDayType, boolean schoolHoliday, int month) {
        this.date = date;
        this.baseDayType = baseDayType;
        this.schoolHoliday = schoolHoliday;
        this.month = month;
    }

    public LocalDate getDate() { return date; }
    public BaseDayType getBaseDayType() { return baseDayType; }
    public boolean isSchoolHoliday() { return schoolHoliday; }
    public int getMonth() { return month; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceDateContext)) return false;
        ServiceDateContext that = (ServiceDateContext) o;
        return schoolHoliday == that.schoolHoliday &&
            month == that.month &&
            Objects.equals(date, that.date) &&
            baseDayType == that.baseDayType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, baseDayType, schoolHoliday, month);
    }

    @Override
    public String toString() {
        return "ServiceDateContext{" +
            "date=" + date +
            ", baseDayType=" + baseDayType +
            ", schoolHoliday=" + schoolHoliday +
            ", month=" + month +
            '}';
    }
}
