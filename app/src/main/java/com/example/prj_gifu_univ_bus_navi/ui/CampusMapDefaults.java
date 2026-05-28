package com.example.prj_gifu_univ_bus_navi.ui;

public final class CampusMapDefaults {
    // 地図画像の表示可能な緯度経度範囲（MapCoordinateTransformerの計算結果に基づく）
    public static final CampusMapBounds bounds = new CampusMapBounds(
        35.47031,  // TOP
        35.45956,  // BOTTOM
        136.73182, // LEFT
        136.74308  // RIGHT
    );

    private CampusMapDefaults() {
    }
}
