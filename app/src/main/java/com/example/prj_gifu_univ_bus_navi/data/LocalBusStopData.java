package com.example.prj_gifu_univ_bus_navi.data;

import com.example.prj_gifu_univ_bus_navi.model.BusStop;
import com.example.prj_gifu_univ_bus_navi.model.BusStopId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class LocalBusStopData {
    private static final List<BusStop> BUS_STOPS = Collections.unmodifiableList(Arrays.asList(
        new BusStop(BusStopId.GIFU_UNIV_HOSPITAL, "岐阜大学病院", "bus_stop_hospital"),
        new BusStop(BusStopId.YANAGIDO, "柳戸橋", "bus_stop_yanagido"),
        new BusStop(BusStopId.GIFU_UNIV, "岐阜大学", "bus_stop_university")
    ));

    private LocalBusStopData() {
    }

    public static List<BusStop> getBusStops() {
        return BUS_STOPS;
    }
}
