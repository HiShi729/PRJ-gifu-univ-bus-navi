package com.example.prj_gifu_univ_bus_navi.ui;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MapCoordinateTransformerTest {
    @Test
    public void testForwardBackwardConversion() {
        double x = 515;
        double y = 502;
        MapCoordinateTransformer.GpsResult gps = MapCoordinateTransformer.pixelToLatLon(x, y);
        MapPoint pixel = MapCoordinateTransformer.latLonToPixel(gps.latitude, gps.longitude);

        // 許容誤差: pixel の往復誤差は 0.01 px 程度
        assertEquals(x, pixel.getX(), 0.01);
        assertEquals(y, pixel.getY(), 0.01);
    }

    @Test
    public void testReferencePoints() {
        // x,y,lat,lon
        // 515,502,35.46746955297617,136.73539905751065
        checkPoint(515, 502, 35.46746955297617, 136.73539905751065);
        // 749,1167,35.463730258992186,136.73697583110183
        checkPoint(749, 1167, 35.463730258992186, 136.73697583110183);
        // 1123,928,35.465091056957434,136.7395811003137
        checkPoint(1123, 928, 35.465091056957434, 136.7395811003137);
        // 1106,1056,35.46434333668635,136.7394602473689 (誤差あり)
        MapCoordinateTransformer.GpsResult res = MapCoordinateTransformer.pixelToLatLon(1106, 1056);
        assertEquals(35.46434333668635, res.latitude, 2e-5); // 誤差を許容 (約2m)
    }

    @Test
    public void testCorners() {
        // LEFT_TOP (0,0)
        MapCoordinateTransformer.GpsResult lt = MapCoordinateTransformer.pixelToLatLon(0, 0);
        assertEquals(35.47025654998892, lt.latitude, 1e-9);
        assertEquals(136.73185155022640, lt.longitude, 1e-9);

        // RIGHT_TOP (1632, 0)
        MapCoordinateTransformer.GpsResult rt = MapCoordinateTransformer.pixelToLatLon(1632, 0);
        assertEquals(35.47030719538238, rt.latitude, 1e-9);
        assertEquals(136.74307924815090, rt.longitude, 1e-9);

        // LEFT_BOTTOM (0, 1904)
        MapCoordinateTransformer.GpsResult lb = MapCoordinateTransformer.pixelToLatLon(0, 1904);
        assertEquals(35.45956239717170, lb.latitude, 1e-9);
        assertEquals(136.73182265236490, lb.longitude, 1e-9);

        // RIGHT_BOTTOM (1632, 1904)
        MapCoordinateTransformer.GpsResult rb = MapCoordinateTransformer.pixelToLatLon(1632, 1904);
        assertEquals(35.45961304256516, rb.latitude, 1e-9);
        assertEquals(136.74305035028942, rb.longitude, 1e-9);
    }

    private void checkPoint(double x, double y, double lat, double lon) {
        MapCoordinateTransformer.GpsResult res = MapCoordinateTransformer.pixelToLatLon(x, y);
        // 基準データとの比較では数m程度の誤差を許容する
        assertEquals(lat, res.latitude, 2e-5);
        assertEquals(lon, res.longitude, 2e-5);
    }
}
