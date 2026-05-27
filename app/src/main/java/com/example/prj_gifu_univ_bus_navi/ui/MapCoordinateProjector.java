package com.example.prj_gifu_univ_bus_navi.ui;

public final class MapCoordinateProjector {
    private MapCoordinateProjector() {
    }

    public static MapPoint project(double latitude, double longitude, float mapWidth, float mapHeight) {
        return project(latitude, longitude, mapWidth, mapHeight, CampusMapDefaults.bounds);
    }

    public static MapPoint project(double latitude, double longitude, float mapWidth, float mapHeight, CampusMapBounds bounds) {
        if (mapWidth <= 0f || mapHeight <= 0f) {
            return null;
        }
        if (!isInBounds(latitude, longitude, bounds)) {
            return null;
        }

        double xRatio = (longitude - bounds.getLeftLongitude()) / (bounds.getRightLongitude() - bounds.getLeftLongitude());
        double yRatio = (bounds.getTopLatitude() - latitude) / (bounds.getTopLatitude() - bounds.getBottomLatitude());
        return new MapPoint(
            (float) (xRatio * mapWidth),
            (float) (yRatio * mapHeight)
        );
    }

    public static boolean isInBounds(double latitude, double longitude) {
        return isInBounds(latitude, longitude, CampusMapDefaults.bounds);
    }

    public static boolean isInBounds(double latitude, double longitude, CampusMapBounds bounds) {
        return latitude <= bounds.getTopLatitude() &&
            latitude >= bounds.getBottomLatitude() &&
            longitude >= bounds.getLeftLongitude() &&
            longitude <= bounds.getRightLongitude();
    }

    public static boolean isGpsLocationVisibleOnCampusMap(Double latitude, Double longitude) {
        return isGpsLocationVisibleOnCampusMap(latitude, longitude, CampusMapDefaults.bounds);
    }

    public static boolean isGpsLocationVisibleOnCampusMap(Double latitude, Double longitude, CampusMapBounds bounds) {
        return latitude != null &&
            longitude != null &&
            isInBounds(latitude, longitude, bounds);
    }
}
