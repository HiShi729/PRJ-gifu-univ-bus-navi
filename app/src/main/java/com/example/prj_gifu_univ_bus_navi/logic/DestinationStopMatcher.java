package com.example.prj_gifu_univ_bus_navi.logic;

import com.example.prj_gifu_univ_bus_navi.model.BusTrip;
import java.time.LocalTime;
import kotlin.Pair;

public final class DestinationStopMatcher {
    private DestinationStopMatcher() {
    }

    public static Pair<String, LocalTime> resolveArrival(BusTrip trip, String selectedDestinationStopName) {
        if ("JR岐阜".equals(selectedDestinationStopName)) {
            LocalTime time = stopTime(trip, "JR岐阜");
            return time == null ? null : new Pair<>("JR岐阜", time);
        }
        if ("名鉄岐阜".equals(selectedDestinationStopName)) {
            LocalTime meitetsu = stopTime(trip, "名鉄岐阜");
            if (meitetsu != null) {
                return new Pair<>("名鉄岐阜", meitetsu);
            }
            LocalTime jr = stopTime(trip, "JR岐阜");
            return jr == null ? null : new Pair<>("JR岐阜", jr);
        }
        LocalTime time = stopTime(trip, selectedDestinationStopName);
        return time == null ? null : new Pair<>(selectedDestinationStopName, time);
    }

    private static LocalTime stopTime(BusTrip trip, String stopName) {
        LocalTime time = trip.getStopTimes().get(stopName);
        if (time != null) {
            return time;
        }
        if ("JR岐阜".equals(stopName)) {
            return trip.getJrGifuArrivalTime();
        }
        if ("名鉄岐阜".equals(stopName)) {
            return trip.getMeitetsuGifuArrivalTime();
        }
        return null;
    }
}
