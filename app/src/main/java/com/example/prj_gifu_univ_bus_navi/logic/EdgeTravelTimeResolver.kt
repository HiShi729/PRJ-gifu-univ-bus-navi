package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.EdgeSourceType
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile
import kotlin.math.ceil

object EdgeTravelTimeResolver {
    private const val RAIN_SCALE_FACTOR = 1.2

    fun resolveMinutes(
        edge: CampusGraphEdge,
        userEdgeOverrides: List<UserEdgeOverride>,
        userTravelTimeProfile: UserTravelTimeProfile?,
        rainModeEnabled: Boolean,
    ): Int {
        val override = userEdgeOverrides.firstOrNull { it.baseEdgeId == edge.id }
        val baseMinutes = when {
            override != null -> override.minutes
            edge.sourceType == EdgeSourceType.STANDARD && userTravelTimeProfile != null -> {
                ceil(edge.minutes * userTravelTimeProfile.timeScaleFactor).toInt()
            }
            else -> edge.minutes
        }.coerceAtLeast(1)

        return if (rainModeEnabled) {
            ceil(baseMinutes * RAIN_SCALE_FACTOR).toInt().coerceAtLeast(1)
        } else {
            baseMinutes
        }
    }
}
