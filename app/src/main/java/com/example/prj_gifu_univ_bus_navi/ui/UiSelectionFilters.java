package com.example.prj_gifu_univ_bus_navi.ui;

import com.example.prj_gifu_univ_bus_navi.model.BusStopCandidate;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode;
import com.example.prj_gifu_univ_bus_navi.model.NodeType;
import java.util.ArrayList;
import java.util.List;

public final class UiSelectionFilters {
    private UiSelectionFilters() {
    }

    public static List<CampusGraphNode> selectableMapNodes(List<CampusGraphNode> nodes) {
        List<CampusGraphNode> result = new ArrayList<>();
        for (CampusGraphNode node : nodes) {
            Double latitude = node.getLatitude();
            Double longitude = node.getLongitude();
            if (latitude == null || longitude == null || !MapCoordinateProjector.isInBounds(latitude, longitude)) {
                continue;
            }
            if (node.getNodeType() == NodeType.BUS_STOP ||
                (node.isSelectableAsStart() && node.getNodeType() != NodeType.TRANSIT)) {
                result.add(node);
            }
        }
        return result;
    }

    public static List<BusStopCandidate> displayCandidatesExcludingRecommended(
        List<BusStopCandidate> allCandidates,
        BusStopCandidate recommendedCandidate
    ) {
        if (recommendedCandidate == null) {
            return allCandidates;
        }
        List<BusStopCandidate> result = new ArrayList<>();
        for (BusStopCandidate candidate : allCandidates) {
            boolean same = candidate.getTripId().equals(recommendedCandidate.getTripId()) &&
                candidate.getBusStopId() == recommendedCandidate.getBusStopId() &&
                candidate.getDepartureTime().equals(recommendedCandidate.getDepartureTime());
            if (!same) {
                result.add(candidate);
            }
        }
        return result;
    }
}
