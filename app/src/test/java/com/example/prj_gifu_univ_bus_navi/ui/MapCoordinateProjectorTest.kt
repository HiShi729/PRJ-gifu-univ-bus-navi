package com.example.prj_gifu_univ_bus_navi.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        assertTrue(MapCoordinateProjector.isInBounds(35.462718, 136.736083))
    }

    @Test
    fun outsideBoundsReturnNull() {
        assertNull(MapCoordinateProjector.project(35.470500, 136.736000, width, height))
        assertNull(MapCoordinateProjector.project(35.464000, 136.742900, width, height))
    }

    @Test
    fun campusAspectRatioIsFinitePositiveValue() {
        val aspectRatio = CampusMapDefaults.bounds.aspectRatio()

        assertTrue(aspectRatio > 0f)
        assertFalse(aspectRatio.isNaN())
        assertFalse(aspectRatio.isInfinite())
    }

    @Test
    fun googleHeadquartersCoordinatesAreOutsideGpsDisplayBounds() {
        assertFalse(MapCoordinateProjector.isInBounds(37.421998333333335, -122.084))
        assertFalse(isGpsLocationVisibleOnCampusMap(37.421998333333335, -122.084))
    }

    @Test
    fun outsideGpsCoordinatesAreNotVisible() {
        assertFalse(isGpsLocationVisibleOnCampusMap(null, 136.736083))
        assertFalse(isGpsLocationVisibleOnCampusMap(35.462718, null))
        assertFalse(isGpsLocationVisibleOnCampusMap(35.470500, 136.736000))
    }
}
