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
            if (override == null) {
                edge.copy(minutes = minutes)
            } else {
                edge.copy(minutes = minutes, sourceType = EdgeSourceType.USER_OVERRIDE)
            }
        }

        val userNodes = userNodeInputs.mapIndexed { index, input ->
            CampusGraphNode(
                id = userNodeId(index, input.name),
                name = input.name,
                nodeType = NodeType.USER_ADDED,
                isSelectableAsStart = input.isSelectableAsStart,
                latitude = input.latitude,
                longitude = input.longitude,
            )
        }
        val userEdges = userNodeInputs.mapIndexed { index, input ->
            CampusGraphEdge(
                id = "user_edge_${index}_${sanitizeId(input.name)}",
                fromNodeId = userNodeId(index, input.name),
                toNodeId = input.connectedNodeId,
                minutes = input.minutesToConnectedNode,
                isBidirectional = true,
                sourceType = EdgeSourceType.USER_ADDED,
                isSelectableForUserEdit = true,
            )
        }

        return Pair(standardNodes + userNodes, overriddenEdges + userEdges)
    }

    private fun userNodeId(index: Int, name: String): String = "user_node_${index}_${sanitizeId(name)}"

    private fun sanitizeId(value: String): String =
        value.lowercase().filter { it.isLetterOrDigit() }.ifBlank { "custom" }
}
