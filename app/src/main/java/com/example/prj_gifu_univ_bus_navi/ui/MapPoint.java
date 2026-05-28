package com.example.prj_gifu_univ_bus_navi.ui;

import java.util.Objects;

public final class MapPoint {
    private final float x;
    private final float y;

    public MapPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapPoint)) return false;
        MapPoint mapPoint = (MapPoint) o;
        return Float.compare(mapPoint.x, x) == 0 && Float.compare(mapPoint.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
