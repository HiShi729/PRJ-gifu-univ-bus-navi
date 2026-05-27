package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.EdgeSourceType
import com.example.prj_gifu_univ_bus_navi.model.NodeType
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile

object CampusGraphBuilder {
    fun buildGraph(
        standardNodes: List<CampusGraphNode>,
        standardEdges: List<CampusGraphEdge>,
        userNodeInputs: List<UserGraphNodeInput>,
        userEdgeOverrides: List<UserEdgeOverride>,
        userTravelTimeProfile: UserTravelTimeProfile? = null,
        rainModeEnabled: Boolean = false,
    ): Pair<List<CampusGraphNode>, List<CampusGraphEdge>> {
        val overrideById = userEdgeOverrides.associateBy { it.baseEdgeId }
        val overriddenEdges = standardEdges.map { edge ->
            val override = overrideById[edge.id]
            val minutes = EdgeTravelTimeResolver.resolveMinutes(
                edge = edge,
                userEdgeOverrides = userEdgeOverrides,
                userTravelTimeProfile = userTravelTimeProfile,
                rainModeEnabled = rainModeEnabled,
            )
            CampusGraphEdge(
                edge.id,
                edge.fromNodeId,
                edge.toNodeId,
                minutes,
                edge.isBidirectional,
                if (override == null) edge.sourceType else EdgeSourceType.USER_OVERRIDE,
                edge.isSelectableForUserEdit,
            )
        }

        val userNodes = userNodeInputs.mapIndexed { index, input ->
            CampusGraphNode(
                userNodeId(index, input.name),
                input.name,
                NodeType.USER_ADDED,
                input.isSelectableAsStart,
                input.latitude,
                input.longitude,
            )
        }
        val userEdges = userNodeInputs.mapIndexed { index, input ->
            CampusGraphEdge(
                "user_edge_${index}_${sanitizeId(input.name)}",
                userNodeId(index, input.name),
                input.connectedNodeId,
                input.minutesToConnectedNode,
                true,
                EdgeSourceType.USER_ADDED,
                true,
            )
        }

        return Pair(standardNodes + userNodes, overriddenEdges + userEdges)
    }

    private fun userNodeId(index: Int, name: String): String = "user_node_${index}_${sanitizeId(name)}"

    private fun sanitizeId(value: String): String =
        value.lowercase().filter { it.isLetterOrDigit() }.ifBlank { "custom" }
}
