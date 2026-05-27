package com.example.prj_gifu_univ_bus_navi.ui

import com.example.prj_gifu_univ_bus_navi.model.BusStopCandidate
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.NodeType

fun selectableMapNodes(nodes: List<CampusGraphNode>): List<CampusGraphNode> =
    nodes.filter { node ->
        node.latitude != null &&
            node.longitude != null &&
            (node.nodeType == NodeType.BUS_STOP || (node.isSelectableAsStart && node.nodeType != NodeType.TRANSIT))
    }

fun displayCandidatesExcludingRecommended(
    allCandidates: List<BusStopCandidate>,
    recommendedCandidate: BusStopCandidate?,
): List<BusStopCandidate> {
    if (recommendedCandidate == null) return allCandidates
    return allCandidates.filterNot { candidate ->
        candidate.tripId == recommendedCandidate.tripId &&
            candidate.busStopId == recommendedCandidate.busStopId &&
            candidate.departureTime == recommendedCandidate.departureTime
    }
}
