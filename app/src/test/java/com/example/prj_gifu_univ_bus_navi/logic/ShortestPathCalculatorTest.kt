package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.data.LocalCampusGraphData
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.EdgeSourceType
import com.example.prj_gifu_univ_bus_navi.model.NodeType
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShortestPathCalculatorTest {
    @Test
    fun returnsTotalMinutesAcrossMultipleEdges() {
        val result = ShortestPathCalculator.findShortestPath(
            LocalCampusGraphData.getNodes(),
            LocalCampusGraphData.getEdges(),
            "engineering_entrance",
            "bus_stop_yanagido",
        )

        assertEquals(9, result?.totalMinutes)
    }

    @Test
    fun returnsNullForUnreachableNode() {
        val isolated = CampusGraphNode("isolated", "孤立地点", NodeType.STANDARD, true, null, null)

        val result = ShortestPathCalculator.findShortestPath(
            LocalCampusGraphData.getNodes() + isolated,
            LocalCampusGraphData.getEdges(),
            "engineering_entrance",
            "isolated",
        )

        assertNull(result)
    }

    @Test
    fun userOverrideMinutesTakePrecedence() {
        val (nodes, edges) = CampusGraphBuilder.buildGraph(
            LocalCampusGraphData.getNodes(),
            LocalCampusGraphData.getEdges(),
            emptyList(),
            listOf(UserEdgeOverride("edge_engineering_common", 1)),
        )

        val result = ShortestPathCalculator.findShortestPath(nodes, edges, "engineering_entrance", "bus_stop_yanagido")

        assertEquals(6, result?.totalMinutes)
    }

    @Test
    fun bidirectionalEdgeCanBeUsedInReverse() {
        val nodes = listOf(
            CampusGraphNode("a", "A", NodeType.STANDARD, true, null, null),
            CampusGraphNode("b", "B", NodeType.STANDARD, true, null, null),
        )
        val edges = listOf(CampusGraphEdge("a_b", "a", "b", 7, true, EdgeSourceType.STANDARD, true))

        val result = ShortestPathCalculator.findShortestPath(nodes, edges, "b", "a")

        assertEquals(7, result?.totalMinutes)
    }
}
