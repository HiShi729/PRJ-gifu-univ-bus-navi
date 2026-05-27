package com.example.prj_gifu_univ_bus_navi.ui;

import java.util.Objects;

public final class CampusMapBounds {
    private final double topLatitude;
    private final double bottomLatitude;
    private final double leftLongitude;
    private final double rightLongitude;

    public CampusMapBounds(double topLatitude, double bottomLatitude, double leftLongitude, double rightLongitude) {
        this.topLatitude = topLatitude;
        this.bottomLatitude = bottomLatitude;
        this.leftLongitude = leftLongitude;
        this.rightLongitude = rightLongitude;
    }

    public double getTopLatitude() {
        return topLatitude;
    }

    public double getBottomLatitude() {
        return bottomLatitude;
    }

    public double getLeftLongitude() {
        return leftLongitude;
    }

    public double getRightLongitude() {
        return rightLongitude;
    }

    public float aspectRatio() {
        double centerLatitudeRadians = Math.toRadians((topLatitude + bottomLatitude) / 2.0);
        double widthScale = (rightLongitude - leftLongitude) * Math.cos(centerLatitudeRadians);
        double heightScale = topLatitude - bottomLatitude;
        if (widthScale <= 0.0 || heightScale <= 0.0) {
            return 1f;
        }
        return (float) (widthScale / heightScale);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CampusMapBounds)) return false;
        CampusMapBounds that = (CampusMapBounds) o;
        return Double.compare(that.topLatitude, topLatitude) == 0 &&
            Double.compare(that.bottomLatitude, bottomLatitude) == 0 &&
            Double.compare(that.leftLongitude, leftLongitude) == 0 &&
            Double.compare(that.rightLongitude, rightLongitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topLatitude, bottomLatitude, leftLongitude, rightLongitude);
    }
}
