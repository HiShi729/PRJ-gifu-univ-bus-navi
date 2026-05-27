package com.example.prj_gifu_univ_bus_navi.ui

data class CampusMapBounds(
    val topLatitude: Double,
    val bottomLatitude: Double,
    val leftLongitude: Double,
    val rightLongitude: Double,
) {
    fun aspectRatio(): Float {
        val centerLatitudeRadians = Math.toRadians((topLatitude + bottomLatitude) / 2.0)
        val widthScale = (rightLongitude - leftLongitude) * kotlin.math.cos(centerLatitudeRadians)
        val heightScale = topLatitude - bottomLatitude
        if (widthScale <= 0.0 || heightScale <= 0.0) return 1f
        return (widthScale / heightScale).toFloat()
    }
}

data class MapPoint(
    val x: Float,
    val y: Float,
)

object CampusMapDefaults {
    val bounds = CampusMapBounds(
        topLatitude = 35.469972,
        bottomLatitude = 35.459833,
        leftLongitude = 136.732333,
        rightLongitude = 136.742528,
    )
}

object MapCoordinateProjector {
    fun project(
        latitude: Double,
        longitude: Double,
        mapWidth: Float,
        mapHeight: Float,
        bounds: CampusMapBounds = CampusMapDefaults.bounds,
    ): MapPoint? {
        if (mapWidth <= 0f || mapHeight <= 0f) return null
        if (!isInBounds(latitude, longitude, bounds)) return null

        val xRatio = (longitude - bounds.leftLongitude) / (bounds.rightLongitude - bounds.leftLongitude)
        val yRatio = (bounds.topLatitude - latitude) / (bounds.topLatitude - bounds.bottomLatitude)
        return MapPoint(
            x = (xRatio * mapWidth).toFloat(),
            y = (yRatio * mapHeight).toFloat(),
        )
    }

    fun isInBounds(
        latitude: Double,
        longitude: Double,
        bounds: CampusMapBounds = CampusMapDefaults.bounds,
    ): Boolean =
        latitude <= bounds.topLatitude &&
            latitude >= bounds.bottomLatitude &&
            longitude >= bounds.leftLongitude &&
            longitude <= bounds.rightLongitude
}

fun isGpsLocationVisibleOnCampusMap(
    latitude: Double?,
    longitude: Double?,
    bounds: CampusMapBounds = CampusMapDefaults.bounds,
): Boolean =
    latitude != null &&
        longitude != null &&
        MapCoordinateProjector.isInBounds(latitude, longitude, bounds)
