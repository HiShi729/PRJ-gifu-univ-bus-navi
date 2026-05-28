package com.example.prj_gifu_univ_bus_navi.ui;

/**
 * 緯度経度からView上の表示座標への投影を行うユーティリティクラス。
 * MapCoordinateTransformer を使用して計算を行います。
 */
public final class MapCoordinateProjector {
    private MapCoordinateProjector() {
    }

    /**
     * 指定された緯度経度を、View上の座標に変換します。
     * 画像がView全体(0,0,mapWidth,mapHeight)に引き伸ばされて表示されていると仮定します。
     */
    public static MapPoint project(double latitude, double longitude, float mapWidth, float mapHeight) {
        if (mapWidth <= 0f || mapHeight <= 0f) {
            return null;
        }
        
        // 1. GPS -> 画像元ピクセル座標
        MapPoint originalPixel = MapCoordinateTransformer.latLonToPixel(latitude, longitude);
        
        // 2. 画像元ピクセル座標 -> View座標
        return MapCoordinateTransformer.imagePixelToViewPoint(originalPixel.getX(), originalPixel.getY(), 0, 0, mapWidth, mapHeight);
    }

    /**
     * 指定された座標が大学敷地内（地図画像が表示可能な範囲）にあるか判定します。
     */
    public static boolean isInBounds(double latitude, double longitude) {
        // 画像の四隅の緯度経度から算出される範囲
        // TOP: 35.4703, BOTTOM: 35.4595, LEFT: 136.7318, RIGHT: 136.7431
        return latitude <= 35.47031 && latitude >= 35.45956 &&
               longitude >= 136.73182 && longitude <= 136.74308;
    }

    public static boolean isGpsLocationVisibleOnCampusMap(Double latitude, Double longitude) {
        return latitude != null && longitude != null && isInBounds(latitude, longitude);
    }
}
