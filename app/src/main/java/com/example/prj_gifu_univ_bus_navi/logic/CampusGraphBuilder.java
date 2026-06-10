package com.example.prj_gifu_univ_bus_navi.logic;

import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode;
import com.example.prj_gifu_univ_bus_navi.model.EdgeSourceType;
import com.example.prj_gifu_univ_bus_navi.model.NodeType;
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride;
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput;
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile;
import java.util.ArrayList;
import java.util.List;
import kotlin.Pair;

public final class CampusGraphBuilder {
    private CampusGraphBuilder() {
    }

    public static Pair<List<CampusGraphNode>, List<CampusGraphEdge>> buildGraph(
        List<CampusGraphNode> standardNodes,
        List<CampusGraphEdge> standardEdges,
        List<UserGraphNodeInput> userNodeInputs,
        List<UserEdgeOverride> userEdgeOverrides,
        UserTravelTimeProfile userTravelTimeProfile,
        boolean rainModeEnabled
    ) {
        List<CampusGraphEdge> overriddenEdges = new ArrayList<>();
        for (CampusGraphEdge edge : standardEdges) {
            UserEdgeOverride override = findOverride(edge.getId(), userEdgeOverrides);
            int seconds = EdgeTravelTimeResolver.resolveSeconds(
                edge,
                userEdgeOverrides,
                userTravelTimeProfile,
                rainModeEnabled
            );
            overriddenEdges.add(new CampusGraphEdge(
                edge.getId(),
                edge.getFromNodeId(),
                edge.getToNodeId(),
                seconds,
                edge.isBidirectional(),
                override == null ? edge.getSourceType() : EdgeSourceType.USER_OVERRIDE,
                edge.isSelectableForUserEdit()
            ));
        }

        List<CampusGraphNode> userNodes = new ArrayList<>();
        List<CampusGraphEdge> userEdges = new ArrayList<>();
        for (int index = 0; index < userNodeInputs.size(); index++) {
            UserGraphNodeInput input = userNodeInputs.get(index);
            String nodeId = userNodeId(index, input.getName());
            userNodes.add(new CampusGraphNode(
                nodeId,
                input.getName(),
                NodeType.USER_ADDED,
                input.isSelectableAsStart(),
                input.getLatitude(),
                input.getLongitude()
            ));
            userEdges.add(new CampusGraphEdge(
                "user_edge_" + index + "_" + sanitizeId(input.getName()),
                nodeId,
                input.getConnectedNodeId(),
                input.getTravelTimeSecondsToConnectedNode(),
                true,
                EdgeSourceType.USER_ADDED,
                true
            ));
        }

        List<CampusGraphNode> nodes = new ArrayList<>(standardNodes);
        nodes.addAll(userNodes);
        List<CampusGraphEdge> edges = new ArrayList<>(overriddenEdges);
        edges.addAll(userEdges);
        return new Pair<>(nodes, edges);
    }

    public static Pair<List<CampusGraphNode>, List<CampusGraphEdge>> buildGraph(
        List<CampusGraphNode> standardNodes,
        List<CampusGraphEdge> standardEdges,
        List<UserGraphNodeInput> userNodeInputs,
        List<UserEdgeOverride> userEdgeOverrides
    ) {
        return buildGraph(standardNodes, standardEdges, userNodeInputs, userEdgeOverrides, null, false);
    }

    private static UserEdgeOverride findOverride(String edgeId, List<UserEdgeOverride> userEdgeOverrides) {
        for (UserEdgeOverride override : userEdgeOverrides) {
            if (override.getBaseEdgeId().equals(edgeId)) {
                return override;
            }
        }
        return null;
    }

    private static String userNodeId(int index, String name) {
        return "user_node_" + index + "_" + sanitizeId(name);
    }

    private static String sanitizeId(String value) {
        StringBuilder builder = new StringBuilder();
        String lower = value.toLowerCase();
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                builder.append(c);
            }
        }
        return builder.length() == 0 ? "custom" : builder.toString();
    }
}
