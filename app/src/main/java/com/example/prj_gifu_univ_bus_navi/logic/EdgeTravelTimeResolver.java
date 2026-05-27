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

    public static int resolveMinutes(
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

        int baseMinutes;
        if (override != null) {
            baseMinutes = override.getMinutes();
        } else if (edge.getSourceType() == EdgeSourceType.STANDARD && userTravelTimeProfile != null) {
            baseMinutes = (int) Math.ceil(edge.getMinutes() * userTravelTimeProfile.getTimeScaleFactor());
        } else {
            baseMinutes = edge.getMinutes();
        }
        baseMinutes = Math.max(baseMinutes, 1);

        if (rainModeEnabled) {
            return Math.max((int) Math.ceil(baseMinutes * RAIN_SCALE_FACTOR), 1);
        }
        return baseMinutes;
    }
}
