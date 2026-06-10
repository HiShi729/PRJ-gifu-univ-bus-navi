package com.example.prj_gifu_univ_bus_navi.logic;

import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode;
import com.example.prj_gifu_univ_bus_navi.model.ShortestPathResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public final class ShortestPathCalculator {
    private ShortestPathCalculator() {
    }

    public static ShortestPathResult findShortestPath(
        List<CampusGraphNode> nodes,
        List<CampusGraphEdge> edges,
        String startNodeId,
        String goalNodeId
    ) {
        Set<String> nodeIds = new HashSet<>();
        for (CampusGraphNode node : nodes) {
            nodeIds.add(node.getId());
        }
        if (!nodeIds.contains(startNodeId) || !nodeIds.contains(goalNodeId)) {
            return null;
        }

        Map<String, List<EdgeTarget>> adjacency = new HashMap<>();
        for (String nodeId : nodeIds) {
            adjacency.put(nodeId, new ArrayList<>());
        }
        for (CampusGraphEdge edge : edges) {
            List<EdgeTarget> fromTargets = adjacency.get(edge.getFromNodeId());
            if (fromTargets != null) {
                fromTargets.add(new EdgeTarget(edge.getToNodeId(), edge.getTravelTimeSeconds()));
            }
            if (edge.isBidirectional()) {
                List<EdgeTarget> toTargets = adjacency.get(edge.getToNodeId());
                if (toTargets != null) {
                    toTargets.add(new EdgeTarget(edge.getFromNodeId(), edge.getTravelTimeSeconds()));
                }
            }
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        for (String nodeId : nodeIds) {
            distances.put(nodeId, Integer.MAX_VALUE);
        }
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingInt(NodeDistance::getDistance));
        distances.put(startNodeId, 0);
        queue.add(new NodeDistance(startNodeId, 0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            Integer knownDistance = distances.get(current.getNodeId());
            if (knownDistance == null || current.getDistance() != knownDistance) {
                continue;
            }
            if (current.getNodeId().equals(goalNodeId)) {
                break;
            }

            List<EdgeTarget> targets = adjacency.get(current.getNodeId());
            if (targets == null) {
                continue;
            }
            for (EdgeTarget target : targets) {
                int newDistance = current.getDistance() + target.getTravelTimeSeconds();
                Integer existingDistance = distances.get(target.getNodeId());
                if (existingDistance != null && newDistance < existingDistance) {
                    distances.put(target.getNodeId(), newDistance);
                    previous.put(target.getNodeId(), current.getNodeId());
                    queue.add(new NodeDistance(target.getNodeId(), newDistance));
                }
            }
        }

        Integer total = distances.get(goalNodeId);
        if (total == null || total == Integer.MAX_VALUE) {
            return null;
        }

        List<String> path = new ArrayList<>();
        String current = goalNodeId;
        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }
        Collections.reverse(path);
        return new ShortestPathResult(total, path);
    }

    private static final class EdgeTarget {
        private final String nodeId;
        private final int travelTimeSeconds;

        private EdgeTarget(String nodeId, int travelTimeSeconds) {
            this.nodeId = nodeId;
            this.travelTimeSeconds = travelTimeSeconds;
        }

        private String getNodeId() {
            return nodeId;
        }

        private int getTravelTimeSeconds() {
            return travelTimeSeconds;
        }
    }

    private static final class NodeDistance {
        private final String nodeId;
        private final int distance;

        private NodeDistance(String nodeId, int distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        private String getNodeId() {
            return nodeId;
        }

        private int getDistance() {
            return distance;
        }
    }
}
