package com.example.prj_gifu_univ_bus_navi.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF labelBounds = new RectF();
    private List<CampusGraphNode> nodes = new ArrayList<>();
    private Location gpsLocation;
    private OnNodeTapListener listener;

    public CampusMapView(Context context) {
        super(context);
        init();
    }

    public CampusMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setColor(Color.rgb(30, 38, 48));
        textPaint.setTextSize(sp(12));
        textPaint.setFakeBoldText(true);
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
        if (nearest != null && nearestDistance <= dp(30)) {
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
            float radius = nodeRadius(node);
            canvas.drawCircle((float) point.getX(), (float) point.getY(), radius, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle((float) point.getX(), (float) point.getY(), Math.max(dp(4), radius * 0.36f), paint);
            drawLabel(canvas, node.getName(), (float) point.getX(), (float) point.getY(), radius, node.getNodeType());
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
        float radius = dp(10);
        canvas.drawCircle((float) point.getX(), (float) point.getY(), radius, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle((float) point.getX(), (float) point.getY(), dp(4), paint);
        drawLabel(canvas, "現在地", (float) point.getX(), (float) point.getY(), radius, null);
    }

    private MapPoint pointFor(CampusGraphNode node) {
        if (node.getLatitude() == null || node.getLongitude() == null) {
            return null;
        }
        return MapCoordinateProjector.project(node.getLatitude(), node.getLongitude(), getWidth(), getHeight());
    }

    private float nodeRadius(CampusGraphNode node) {
        if (node.getNodeType() == NodeType.BUS_STOP) {
            return dp(15);
        }
        if (node.getNodeType() == NodeType.USER_ADDED) {
            return dp(13);
        }
        return dp(12);
    }

    private void drawLabel(Canvas canvas, String text, float x, float y, float radius, NodeType nodeType) {
        if (text == null || text.trim().isEmpty()) return;

        float paddingX = dp(7);
        float paddingY = dp(4);
        float textWidth = textPaint.measureText(text);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float labelWidth = textWidth + paddingX * 2;
        float labelHeight = metrics.descent - metrics.ascent + paddingY * 2;
        float left = x + radius + dp(5);
        float top = y - radius - labelHeight + dp(2);

        if (left + labelWidth > getWidth() - dp(4)) {
            left = x - radius - dp(5) - labelWidth;
        }
        if (left < dp(4)) {
            left = dp(4);
        }
        if (top < dp(4)) {
            top = y + radius + dp(5);
        }
        if (top + labelHeight > getHeight() - dp(4)) {
            top = getHeight() - dp(4) - labelHeight;
        }

        labelBounds.set(left, top, left + labelWidth, top + labelHeight);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(nodeType == NodeType.BUS_STOP ? Color.argb(232, 255, 241, 238) : Color.argb(232, 255, 255, 255));
        canvas.drawRoundRect(labelBounds, dp(6), dp(6), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(nodeType == NodeType.BUS_STOP ? Color.rgb(221, 124, 113) : Color.rgb(185, 194, 205));
        canvas.drawRoundRect(labelBounds, dp(6), dp(6), paint);
        canvas.drawText(text, left + paddingX, top + paddingY - metrics.ascent, textPaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
