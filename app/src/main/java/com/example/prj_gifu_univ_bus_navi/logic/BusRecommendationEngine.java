package com.example.prj_gifu_univ_bus_navi.logic;

import com.example.prj_gifu_univ_bus_navi.model.BusStop;
import com.example.prj_gifu_univ_bus_navi.model.BusStopCandidate;
import com.example.prj_gifu_univ_bus_navi.model.BusStopId;
import com.example.prj_gifu_univ_bus_navi.model.BusTrip;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode;
import com.example.prj_gifu_univ_bus_navi.model.RecommendationResult;
import com.example.prj_gifu_univ_bus_navi.model.ServiceDateContext;
import com.example.prj_gifu_univ_bus_navi.model.ShortestPathResult;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.Pair;

public final class BusRecommendationEngine {
    private static final String NO_CANDIDATE_MESSAGE = "現在時刻以降に乗車可能な便がありません";

    private BusRecommendationEngine() {
    }

    public static RecommendationResult recommend(
        String currentNodeId,
        LocalDate nowDate,
        LocalTime nowTime,
        int safetyMarginMinutes,
        String selectedDestinationStopName,
        List<BusTrip> busTrips,
        List<BusStop> busStops,
        List<CampusGraphNode> graphNodes,
        List<CampusGraphEdge> graphEdges,
        List<LocalDate> schoolHolidays
    ) {
        ServiceDateContext context = ServiceCalendar.createContext(nowDate, schoolHolidays);
        List<BusTrip> availableTrips = BusTripServiceFilter.filterAvailableTrips(busTrips, context);
        Map<BusStop, ShortestPathResult> pathsByStop = new HashMap<>();
        for (BusStop busStop : busStops) {
            pathsByStop.put(
                busStop,
                ShortestPathCalculator.findShortestPath(graphNodes, graphEdges, currentNodeId, busStop.getNodeId())
            );
        }

        List<BusStopCandidate> candidates = new ArrayList<>();
        for (BusTrip trip : availableTrips) {
            Pair<String, LocalTime> arrival = DestinationStopMatcher.resolveArrival(trip, selectedDestinationStopName);
            if (arrival == null) {
                continue;
            }
            for (BusStop busStop : busStops) {
                ShortestPathResult path = pathsByStop.get(busStop);
                if (path == null) {
                    continue;
                }
                LocalTime departureTime = departureTimeAt(trip, busStop.getId());
                if (departureTime == null) {
                    continue;
                }
                BusStopCandidate candidate = createCandidate(
                    trip,
                    busStop,
                    departureTime,
                    path.getTotalSeconds(),
                    nowTime,
                    safetyMarginMinutes,
                    selectedDestinationStopName,
                    arrival
                );
                if (candidate != null) {
                    candidates.add(candidate);
                }
            }
        }
        candidates.sort(Comparator.comparing(BusStopCandidate::getDepartureTime).thenComparingInt(BusStopCandidate::getTravelTimeSeconds));

        BusStopCandidate recommended = candidates.stream()
            .min(Comparator.comparing(BusStopCandidate::getDepartureTime).thenComparingInt(BusStopCandidate::getTravelTimeSeconds))
            .orElse(null);

        return new RecommendationResult(
            recommended,
            candidates.subList(0, Math.min(10, candidates.size())),
            recommended == null ? createSummaryOnlyCandidates(pathsByStop, nowTime, selectedDestinationStopName) : candidates,
            recommended == null ? NO_CANDIDATE_MESSAGE : null
        );
    }

    private static List<BusStopCandidate> createSummaryOnlyCandidates(
        Map<BusStop, ShortestPathResult> pathsByStop,
        LocalTime nowTime,
        String selectedDestinationStopName
    ) {
        List<BusStopCandidate> summaryCandidates = new ArrayList<>();
        for (Map.Entry<BusStop, ShortestPathResult> entry : pathsByStop.entrySet()) {
            BusStop busStop = entry.getKey();
            ShortestPathResult path = entry.getValue();
            if (path == null) {
                continue;
            }
            int travelTimeSeconds = path.getTotalSeconds();
            summaryCandidates.add(new BusStopCandidate(
                busStop.getId(),
                busStop.getName(),
                "",
                null,
                travelTimeSeconds,
                nowTime.plusSeconds(travelTimeSeconds),
                0,
                false,
                "",
                selectedDestinationStopName,
                null,
                null,
                false,
                "",
                NO_CANDIDATE_MESSAGE
            ));
        }
        return summaryCandidates;
    }

    private static BusStopCandidate createCandidate(
        BusTrip trip,
        BusStop busStop,
        LocalTime departureTime,
        int travelTimeSeconds,
        LocalTime nowTime,
        int safetyMarginMinutes,
        String selectedDestinationStopName,
        Pair<String, LocalTime> resolvedArrival
    ) {
        LocalTime arrivalAtBusStop = nowTime.plusSeconds(travelTimeSeconds);
        LocalTime latestArrivalWithMargin = arrivalAtBusStop.plusMinutes(safetyMarginMinutes);
        boolean canCatch = !latestArrivalWithMargin.isAfter(departureTime);
        int remainingMinutes = (int) Duration.between(latestArrivalWithMargin, departureTime).toMinutes();
        if (remainingMinutes < 0 || !canCatch) {
            return null;
        }
        return new BusStopCandidate(
            busStop.getId(),
            busStop.getName(),
            trip.getId(),
            departureTime,
            travelTimeSeconds,
            arrivalAtBusStop,
            remainingMinutes,
            true,
            trip.getRouteName(),
            selectedDestinationStopName,
            resolvedArrival.getFirst(),
            resolvedArrival.getSecond(),
            trip.isMayBeArticulatedBus(),
            trip.getOptionLabel(),
            "発車時刻までに到着でき、候補の中で発車時刻と移動時間を比較できます"
        );
    }

    private static LocalTime departureTimeAt(BusTrip trip, BusStopId busStopId) {
        switch (busStopId) {
            case GIFU_UNIV_HOSPITAL:
                return trip.getHospitalDepartureTime();
            case YANAGIDO:
                return trip.getYanagidoDepartureTime();
            case GIFU_UNIV:
                return trip.getUniversityDepartureTime();
            default:
                return null;
        }
    }
}
