package com.example.prj_gifu_univ_bus_navi.ui

import com.example.prj_gifu_univ_bus_navi.model.BusStopCandidate
import com.example.prj_gifu_univ_bus_navi.model.BusStopId
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.NodeType
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput
import com.example.prj_gifu_univ_bus_navi.model.UserNodeCoordinateSource
import com.example.prj_gifu_univ_bus_navi.logic.CampusGraphBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class UiSelectionFiltersTest {
    @Test
    fun selectableMapNodesIncludeSelectableStandardNodesAndBusStopsWithCoordinates() {
        val nodes = listOf(
            node("selectable_standard_with_coords", NodeType.STANDARD, selectable = true, latitude = 35.4640, longitude = 136.7350),
            node("not_selectable_standard_with_coords", NodeType.STANDARD, selectable = false, latitude = 35.4641, longitude = 136.7351),
            node("selectable_without_coords", NodeType.STANDARD, selectable = true, latitude = null, longitude = null),
            node("bus_stop_with_coords", NodeType.BUS_STOP, selectable = false, latitude = 35.467137, longitude = 136.735476),
            node("outside_bounds", NodeType.BUS_STOP, selectable = false, latitude = 35.4800, longitude = 136.7350),
        )

        val result = selectableMapNodes(nodes)

        assertEquals(listOf("selectable_standard_with_coords", "bus_stop_with_coords"), result.map { it.id })
    }

    @Test
    fun userAddedNodeWithoutCoordinatesIsNotMapSelectable() {
        val graph = CampusGraphBuilder.buildGraph(
            emptyList(),
            emptyList(),
            listOf(
                UserGraphNodeInput(
                    "研究室",
                    "base",
                    3,
                    true,
                ),
            ),
            emptyList(),
        )
        val nodes = graph.first

        assertTrue(selectableMapNodes(nodes).isEmpty())
    }

    @Test
    fun userAddedNodeWithManualCoordinatesIsMapSelectableAndKeepsCoordinates() {
        val graph = CampusGraphBuilder.buildGraph(
            emptyList(),
            emptyList(),
            listOf(
                UserGraphNodeInput(
                    "研究室",
                    "base",
                    3,
                    true,
                    35.4645,
                    136.7355,
                    UserNodeCoordinateSource.MANUAL,
                ),
            ),
            emptyList(),
        )
        val nodes = graph.first

        val result = selectableMapNodes(nodes)

        assertEquals(1, result.size)
        assertEquals(35.4645, result.single().latitude ?: 0.0, 0.0001)
        assertEquals(136.7355, result.single().longitude ?: 0.0, 0.0001)
    }

    @Test
    fun viewModelMapPinTapUpdatesCurrentNodeIdRunsRecommendationAndShowsResult() {
        val viewModel = MainViewModel()
        val target = viewModel.mapSelectableNodes.first { it.id != viewModel.selectedCurrentNodeId }

        viewModel.selectMapNodeAndRecommend(target.id)

        assertEquals(target.id, viewModel.selectedCurrentNodeId)
        assertEquals(AppScreen.RESULT, viewModel.currentScreen)
        assertNotNull(viewModel.recommendationResult)
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
        nodeType: NodeType,
        selectable: Boolean,
        latitude: Double?,
        longitude: Double?,
    ) = CampusGraphNode(
        id,
        id,
        nodeType,
        selectable,
        latitude,
        longitude,
    )

    private fun candidate(
        tripId: String,
        busStopId: BusStopId,
        departureTime: LocalTime,
    ) = BusStopCandidate(
        busStopId,
        busStopId.name,
        tripId,
        departureTime,
        5,
        departureTime.minusMinutes(5),
        5,
        true,
        "テスト",
        "JR岐阜",
        "JR岐阜",
        departureTime.plusMinutes(30),
        false,
        "テスト",
    )
}
