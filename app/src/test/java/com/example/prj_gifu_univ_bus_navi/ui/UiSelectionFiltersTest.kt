package com.example.prj_gifu_univ_bus_navi.ui

import com.example.prj_gifu_univ_bus_navi.model.BusStopCandidate
import com.example.prj_gifu_univ_bus_navi.model.BusStopId
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.DestinationBusStop
import com.example.prj_gifu_univ_bus_navi.model.NodeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class UiSelectionFiltersTest {
    @Test
    fun selectableMapNodesIncludeOnlyStartSelectableNodesWithCoordinates() {
        val nodes = listOf(
            node("selectable_with_coords", selectable = true, latitude = 35.0, longitude = 136.0),
            node("not_selectable_with_coords", selectable = false, latitude = 35.1, longitude = 136.1),
            node("selectable_without_coords", selectable = true, latitude = null, longitude = null),
        )

        val result = selectableMapNodes(nodes)

        assertEquals(listOf("selectable_with_coords"), result.map { it.id })
    }

    @Test
    fun viewModelMapSelectionUpdatesCurrentNodeId() {
        val viewModel = MainViewModel()
        val target = viewModel.mapSelectableNodes.first { it.id != viewModel.selectedCurrentNodeId }

        viewModel.selectMapNode(target.id)
        viewModel.confirmMapNodeSelection()

        assertEquals(target.id, viewModel.selectedCurrentNodeId)
        assertEquals(AppScreen.HOME, viewModel.currentScreen)
    }

    @Test
    fun displayCandidatesExcludeRecommendedCandidateWithSameTripStopAndDeparture() {
        val recommended = candidate("trip-a", BusStopId.YANAGIDO, LocalTime.of(18, 8))
        val same = candidate("trip-a", BusStopId.YANAGIDO, LocalTime.of(18, 8))
        val different = candidate("trip-a", BusStopId.GIFU_UNIV, LocalTime.of(18, 10))

        val result = displayCandidatesExcludingRecommended(listOf(same, different), recommended)

        assertFalse(result.contains(same))
        assertTrue(result.contains(different))
    }

    @Test
    fun displayCandidatesRemainUnchangedWhenRecommendedIsNull() {
        val candidates = listOf(
            candidate("trip-a", BusStopId.YANAGIDO, LocalTime.of(18, 8)),
            candidate("trip-b", BusStopId.GIFU_UNIV, LocalTime.of(18, 10)),
        )

        assertEquals(candidates, displayCandidatesExcludingRecommended(candidates, null))
    }

    private fun node(
        id: String,
        selectable: Boolean,
        latitude: Double?,
        longitude: Double?,
    ) = CampusGraphNode(
        id = id,
        name = id,
        nodeType = NodeType.STANDARD,
        isSelectableAsStart = selectable,
        latitude = latitude,
        longitude = longitude,
    )

    private fun candidate(
        tripId: String,
        busStopId: BusStopId,
        departureTime: LocalTime,
    ) = BusStopCandidate(
        busStopId = busStopId,
        busStopName = busStopId.name,
        tripId = tripId,
        departureTime = departureTime,
        travelMinutes = 5,
        arrivalTimeAtBusStop = departureTime.minusMinutes(5),
        remainingMinutes = 5,
        canCatch = true,
        routeName = "テスト",
        destinationBusStop = DestinationBusStop.JR_GIFU,
        actualArrivalBusStop = DestinationBusStop.JR_GIFU,
        destinationArrivalTime = departureTime.plusMinutes(30),
        mayBeArticulatedBus = false,
        reason = "テスト",
    )
}
