package com.example.prj_gifu_univ_bus_navi.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MapCoordinateProjectorTest {
    private val width = 1000f
    private val height = 800f

    @Test
    fun topLeftBoundsProjectNearOrigin() {
        val point = MapCoordinateProjector.project(
            latitude = CampusMapDefaults.bounds.topLatitude,
            longitude = CampusMapDefaults.bounds.leftLongitude,
            mapWidth = width,
            mapHeight = height,
        )

        assertNotNull(point)
        assertEquals(0f, point!!.x, 0.001f)
        assertEquals(0f, point.y, 0.001f)
    }

    @Test
    fun bottomRightBoundsProjectNearMapSize() {
        val point = MapCoordinateProjector.project(
            latitude = CampusMapDefaults.bounds.bottomLatitude,
            longitude = CampusMapDefaults.bounds.rightLongitude,
            mapWidth = width,
            mapHeight = height,
        )

        assertNotNull(point)
        assertEquals(width, point!!.x, 0.001f)
        assertEquals(height, point.y, 0.001f)
    }

    @Test
    fun busStopCoordinatesAreInsideBounds() {
        assertNotNull(MapCoordinateProjector.project(35.462718, 136.736083, width, height))
        assertNotNull(MapCoordinateProjector.project(35.467137, 136.735476, width, height))
        assertNotNull(MapCoordinateProjector.project(35.467718, 136.732805, width, height))
    }

    @Test
    fun outsideBoundsReturnNull() {
        assertNull(MapCoordinateProjector.project(35.470500, 136.736000, width, height))
        assertNull(MapCoordinateProjector.project(35.464000, 136.742900, width, height))
    }
}
