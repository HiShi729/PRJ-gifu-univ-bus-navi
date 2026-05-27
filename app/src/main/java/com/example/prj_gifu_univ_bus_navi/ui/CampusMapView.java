package com.example.prj_gifu_univ_bus_navi.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode;
import com.example.prj_gifu_univ_bus_navi.model.NodeType;
import java.util.ArrayList;
import java.util.List;

public final class CampusMapView extends View {
    public interface OnNodeTapListener {
        void onNodeTapped(CampusGraphNode node);
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<CampusGraphNode> nodes = new ArrayList<>();
    private Location gpsLocation;
    private OnNodeTapListener listener;

    public CampusMapView(Context context) {
        super(context);
    }

    public CampusMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNodes(List<CampusGraphNode> nodes) {
        this.nodes = new ArrayList<>(nodes);
        invalidate();
    }

    public void setGpsLocation(Location gpsLocation) {
        this.gpsLocation = gpsLocation;
        invalidate();
    }

    public void setOnNodeTapListener(OnNodeTapListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width / CampusMapDefaults.bounds.aspectRatio());
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawNodes(canvas);
        drawGps(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || listener == null) {
            return true;
        }
        CampusGraphNode nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (CampusGraphNode node : nodes) {
            MapPoint point = pointFor(node);
            if (point == null) continue;
            double distance = Math.hypot(point.getX() - event.getX(), point.getY() - event.getY());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = node;
            }
        }
        if (nearest != null && nearestDistance <= 48.0) {
            listener.onNodeTapped(nearest);
        }
        return true;
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.rgb(239, 243, 236));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(216, 227, 243));
        canvas.drawRoundRect(getWidth() * 0.13f, getHeight() * 0.16f, getWidth() * 0.33f, getHeight() * 0.32f, 10f, 10f, paint);
        paint.setColor(Color.rgb(241, 229, 201));
        canvas.drawRoundRect(getWidth() * 0.44f, getHeight() * 0.18f, getWidth() * 0.66f, getHeight() * 0.32f, 10f, 10f, paint);
        paint.setColor(Color.rgb(220, 234, 213));
        canvas.drawRoundRect(getWidth() * 0.52f, getHeight() * 0.50f, getWidth() * 0.80f, getHeight() * 0.70f, 10f, 10f, paint);
        paint.setColor(Color.rgb(240, 214, 214));
        canvas.drawRoundRect(getWidth() * 0.10f, getHeight() * 0.62f, getWidth() * 0.32f, getHeight() * 0.80f, 10f, 10f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
        paint.setColor(Color.rgb(150, 160, 150));
        canvas.drawLine(getWidth() * 0.08f, getHeight() * 0.40f, getWidth() * 0.92f, getHeight() * 0.45f, paint);
        canvas.drawLine(getWidth() * 0.36f, getHeight() * 0.08f, getWidth() * 0.58f, getHeight() * 0.88f, paint);
    }

    private void drawNodes(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (CampusGraphNode node : nodes) {
            MapPoint point = pointFor(node);
            if (point == null) continue;
            if (node.getNodeType() == NodeType.BUS_STOP) {
                paint.setColor(Color.rgb(196, 64, 55));
            } else if (node.getNodeType() == NodeType.USER_ADDED) {
                paint.setColor(Color.rgb(49, 111, 173));
            } else {
                paint.setColor(Color.rgb(57, 82, 120));
            }
            canvas.drawCircle((float) point.getX(), (float) point.getY(), 11f, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle((float) point.getX(), (float) point.getY(), 4f, paint);
        }
    }

    private void drawGps(Canvas canvas) {
        if (gpsLocation == null ||
            !MapCoordinateProjector.isGpsLocationVisibleOnCampusMap(gpsLocation.getLatitude(), gpsLocation.getLongitude())) {
            return;
        }
        MapPoint point = MapCoordinateProjector.project(gpsLocation.getLatitude(), gpsLocation.getLongitude(), getWidth(), getHeight());
        if (point == null) return;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(34, 160, 78));
        canvas.drawCircle((float) point.getX(), (float) point.getY(), 12f, paint);
    }

    private MapPoint pointFor(CampusGraphNode node) {
        if (node.getLatitude() == null || node.getLongitude() == null) {
            return null;
        }
        return MapCoordinateProjector.project(node.getLatitude(), node.getLongitude(), getWidth(), getHeight());
    }
}
