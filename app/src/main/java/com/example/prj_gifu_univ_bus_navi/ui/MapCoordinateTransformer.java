package com.example.prj_gifu_univ_bus_navi.ui;

/**
 * 地図画像の元ピクセル座標と緯度経度の相互変換を行うクラス。
 * 8点のアフィン変換係数を使用し、高精度な変換を提供します。
 *
 * 画像サイズが変わった場合は、アフィン係数(A-F)を再計算する必要があります。
 */
public final class MapCoordinateTransformer {
    // 元画像サイズ (tatemono_no_number.png)
    public static final int ORIGINAL_WIDTH = 1632;
    public static final int ORIGINAL_HEIGHT = 1904;

    // アフィン変換係数 (lat = A*x + B*y + C, lon = D*x + E*y + F)
    private static final double A = 3.10327166e-8;
    private static final double B = -5.61667690e-6;
    private static final double C = 35.47025654998892;
    private static final double D = 6.87971687e-6;
    private static final double E = -1.51774483e-8;
    private static final double F = 136.7318515502264;

    // 逆変換用行列式
    private static final double DET = A * E - B * D;

    private MapCoordinateTransformer() {
    }

    /**
     * 画像の元ピクセル座標から緯度経度へ変換します。
     */
    public static GpsResult pixelToLatLon(double x, double y) {
        double lat = A * x + B * y + C;
        double lon = D * x + E * y + F;
        return new GpsResult(lat, lon);
    }

    /**
     * 緯度経度から画像の元ピクセル座標へ変換します。
     */
    public static MapPoint latLonToPixel(double lat, double lon) {
        if (Math.abs(DET) < 1e-18) {
            return new MapPoint(0, 0); // 変換不能
        }
        double x = (E * (lat - C) - B * (lon - F)) / DET;
        double y = (-D * (lat - C) + A * (lon - F)) / DET;
        return new MapPoint((float) x, (float) y);
    }

    /**
     * 画像の元ピクセル座標から、View上の表示座標へ変換します。
     */
    public static MapPoint imagePixelToViewPoint(float imagePixelX, float imagePixelY, float viewLeft, float viewTop, float viewWidth, float viewHeight) {
        float viewX = viewLeft + imagePixelX * viewWidth / ORIGINAL_WIDTH;
        float viewY = viewTop + imagePixelY * viewHeight / ORIGINAL_HEIGHT;
        return new MapPoint(viewX, viewY);
    }

    /**
     * View上の座標から、画像の元ピクセル座標へ変換します。
     */
    public static MapPoint viewPointToImagePixel(float viewX, float viewY, float viewLeft, float viewTop, float viewWidth, float viewHeight) {
        if (viewWidth == 0 || viewHeight == 0) {
            return new MapPoint(0, 0);
        }
        float imagePixelX = (viewX - viewLeft) * ORIGINAL_WIDTH / viewWidth;
        float imagePixelY = (viewY - viewTop) * ORIGINAL_HEIGHT / viewHeight;
        return new MapPoint(imagePixelX, imagePixelY);
    }

    public static final class GpsResult {
        public final double latitude;
        public final double longitude;

        public GpsResult(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
