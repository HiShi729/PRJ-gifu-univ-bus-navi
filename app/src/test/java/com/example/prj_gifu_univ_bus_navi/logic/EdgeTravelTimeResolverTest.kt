package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.EdgeSourceType
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class EdgeTravelTimeResolverTest {
    private val edge = CampusGraphEdge("edge", "a", "b", 5, true, EdgeSourceType.STANDARD, true)
    private val profile = UserTravelTimeProfile("edge", 5, 3, 0.6)

    @Test
    fun overrideTakesPrecedenceOverProfile() {
        assertEquals(2, EdgeTravelTimeResolver.resolveMinutes(edge, listOf(UserEdgeOverride("edge", 2)), profile, rainModeEnabled = false))
    }

    @Test
    fun profileScalesStandardEdgeAndCeils() {
        assertEquals(3, EdgeTravelTimeResolver.resolveMinutes(edge, emptyList(), profile, rainModeEnabled = false))
        assertEquals(4, EdgeTravelTimeResolver.resolveMinutes(edge, emptyList(), UserTravelTimeProfile("edge", 5, 3, 0.61), rainModeEnabled = false))
    }

    @Test
    fun usesStandardMinutesWithoutProfile() {
        assertEquals(5, EdgeTravelTimeResolver.resolveMinutes(edge, emptyList(), null, rainModeEnabled = false))
    }

    @Test
    fun rainModeScalesResolvedMinutes() {
        assertEquals(4, EdgeTravelTimeResolver.resolveMinutes(edge, emptyList(), profile, rainModeEnabled = true))
    }
}
