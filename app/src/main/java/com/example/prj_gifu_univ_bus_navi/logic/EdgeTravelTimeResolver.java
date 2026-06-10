package com.example.prj_gifu_univ_bus_navi.logic;

import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge;
import com.example.prj_gifu_univ_bus_navi.model.EdgeSourceType;
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride;
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile;
import java.util.List;

public final class EdgeTravelTimeResolver {
    private static final double RAIN_SCALE_FACTOR = 1.2;

    private EdgeTravelTimeResolver() {
    }

    public static int resolveSeconds(
        CampusGraphEdge edge,
        List<UserEdgeOverride> userEdgeOverrides,
        UserTravelTimeProfile userTravelTimeProfile,
        boolean rainModeEnabled
    ) {
        UserEdgeOverride override = null;
        for (UserEdgeOverride candidate : userEdgeOverrides) {
            if (candidate.getBaseEdgeId().equals(edge.getId())) {
                override = candidate;
                break;
            }
        }

        int baseSeconds;
        if (override != null) {
            baseSeconds = override.getTravelTimeSeconds();
        } else if (edge.getSourceType() == EdgeSourceType.STANDARD && userTravelTimeProfile != null) {
            baseSeconds = (int) Math.ceil(edge.getTravelTimeSeconds() * userTravelTimeProfile.getTimeScaleFactor());
        } else {
            baseSeconds = edge.getTravelTimeSeconds();
        }
        baseSeconds = Math.max(baseSeconds, 1);

        if (rainModeEnabled) {
            return Math.max((int) Math.ceil(baseSeconds * RAIN_SCALE_FACTOR), 1);
        }
        return baseSeconds;
    }
}
