package com.example.prj_gifu_univ_bus_navi.logic;

import com.example.prj_gifu_univ_bus_navi.model.BusTrip;
import com.example.prj_gifu_univ_bus_navi.model.OperationRule;
import com.example.prj_gifu_univ_bus_navi.model.ServiceDateContext;
import java.util.ArrayList;
import java.util.List;

public final class BusTripServiceFilter {
    private BusTripServiceFilter() {
    }

    public static boolean isTripAvailable(BusTrip trip, ServiceDateContext context) {
        if (trip.getBaseDayType() != context.getBaseDayType()) {
            return false;
        }
        OperationRule rule = trip.getOperationRule();
        switch (rule) {
            case NONE:
                return true;
            case SCHOOL_HOLIDAY_EXCLUDED:
                return !context.isSchoolHoliday();
            case SCHOOL_HOLIDAY_ONLY:
                return context.isSchoolHoliday();
            case LIMITED_PERIOD:
                return isInOperatingMonth(trip, context.getMonth());
            case LIMITED_PERIOD_AND_SCHOOL_HOLIDAY_EXCLUDED:
                return isInOperatingMonth(trip, context.getMonth()) && !context.isSchoolHoliday();
            default:
                return false;
        }
    }

    public static List<BusTrip> filterAvailableTrips(List<BusTrip> trips, ServiceDateContext context) {
        List<BusTrip> available = new ArrayList<>();
        for (BusTrip trip : trips) {
            if (isTripAvailable(trip, context)) {
                available.add(trip);
            }
        }
        return available;
    }

    private static boolean isInOperatingMonth(BusTrip trip, int month) {
        Integer start = trip.getOperatingStartMonth();
        Integer end = trip.getOperatingEndMonth();
        if (start == null || end == null) {
            return false;
        }
        if (start <= end) {
            return month >= start && month <= end;
        }
        return month >= start || month <= end;
    }
}
