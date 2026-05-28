package com.example.prj_gifu_univ_bus_navi.model;

import java.time.LocalTime;
import java.util.Objects;

public final class StopTime {
    private final String stopName;
    private final LocalTime time;

    public StopTime(String stopName, LocalTime time) {
        this.stopName = stopName;
        this.time = time;
    }

    public String getStopName() {
        return stopName;
    }

    public LocalTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StopTime)) return false;
        StopTime stopTime = (StopTime) o;
        return Objects.equals(stopName, stopTime.stopName) && Objects.equals(time, stopTime.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopName, time);
    }

    @Override
    public String toString() {
        return "StopTime{" +
            "stopName='" + stopName + '\'' +
            ", time=" + time +
            '}';
    }
}
