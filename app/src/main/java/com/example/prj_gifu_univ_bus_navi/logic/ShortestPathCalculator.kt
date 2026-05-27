package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.ShortestPathResult
import java.util.PriorityQueue

object ShortestPathCalculator {
    fun findShortestPath(
        nodes: List<CampusGraphNode>,
        edges: List<CampusGraphEdge>,
        startNodeId: String,
        goalNodeId: String,
    ): ShortestPathResult? {
        val nodeIds = nodes.map { it.id }.toSet()
        if (startNodeId !in nodeIds || goalNodeId !in nodeIds) return null

        val adjacency = buildMap<String, MutableList<Pair<String, Int>>> {
            nodeIds.forEach { put(it, mutableListOf()) }
            edges.forEach { edge ->
                getValue(edge.fromNodeId).add(edge.toNodeId to edge.minutes)
                if (edge.isBidirectional) {
                    getValue(edge.toNodeId).add(edge.fromNodeId to edge.minutes)
                }
            }
        }

        val distances = nodeIds.associateWith { Int.MAX_VALUE }.toMutableMap()
        val previous = mutableMapOf<String, String>()
        val queue = PriorityQueue(compareBy<Pair<String, Int>> { it.second })
        distances[startNodeId] = 0
        queue.add(startNodeId to 0)

        while (queue.isNotEmpty()) {
            val (current, currentDistance) = queue.poll() ?: break
            if (currentDistance != distances[current]) continue
            if (current == goalNodeId) break

            adjacency.getValue(current).forEach { (next, minutes) ->
                val newDistance = currentDistance + minutes
                if (newDistance < distances.getValue(next)) {
                    distances[next] = newDistance
                    previous[next] = current
                    queue.add(next to newDistance)
                }
            }
        }

        val total = distances[goalNodeId] ?: Int.MAX_VALUE
        if (total == Int.MAX_VALUE) return null

        val path = buildList {
            var current: String? = goalNodeId
            while (current != null) {
                add(current)
                current = previous[current]
            }
        }.asReversed()

        return ShortestPathResult(total, path)
    }
}
