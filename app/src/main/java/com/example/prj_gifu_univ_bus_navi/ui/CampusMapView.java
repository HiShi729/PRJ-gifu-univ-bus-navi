package com.example.prj_gifu_univ_bus_navi.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.prj_gifu_univ_bus_navi.R;
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
    private Bitmap backgroundBitmap;
    private String selectedNodeId;

    public CampusMapView(Context context) {
        super(context);
        init();
    }

    public CampusMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        try {
            backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tatemono_no_number);
        } catch (Exception e) {
            // Ignore
        }
    }

    public void setNodes(List<CampusGraphNode> nodes) {
        this.nodes = new ArrayList<>(nodes);
        invalidate();
    }

    public void setSelectedNodeId(String selectedNodeId) {
        this.selectedNodeId = selectedNodeId;
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
        float aspectRatio = (float) MapCoordinateTransformer.ORIGINAL_WIDTH / MapCoordinateTransformer.ORIGINAL_HEIGHT;
        int height = (int) (width / aspectRatio);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMapBackground(canvas);
        drawPinsAndLabels(canvas);
    }

    private void drawMapBackground(Canvas canvas) {
        if (backgroundBitmap != null) {
            Rect src = new Rect(0, 0, backgroundBitmap.getWidth(), backgroundBitmap.getHeight());
            Rect dst = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(backgroundBitmap, src, dst, paint);
        } else {
            canvas.drawColor(Color.rgb(245, 247, 242));
        }
    }

    private void drawPinsAndLabels(Canvas canvas) {
        // 1. Draw Selectable Nodes (excluding Bus Stops and Selected)
        paint.setStyle(Paint.Style.FILL);
        for (CampusGraphNode node : nodes) {
            if (node.getId().equals(selectedNodeId)) continue;
            if (node.getNodeType() == NodeType.BUS_STOP) continue;

            MapPoint point = pointFor(node);
            if (point == null) continue;
            drawPin(canvas, (float) point.getX(), (float) point.getY(), nodeRadius(node), Color.BLACK, Color.WHITE, false);
        }

        // 2. Draw Bus Stops (excluding Selected)
        for (CampusGraphNode node : nodes) {
            if (node.getId().equals(selectedNodeId)) continue;
            if (node.getNodeType() != NodeType.BUS_STOP) continue;

            MapPoint point = pointFor(node);
            if (point == null) continue;
            drawPin(canvas, (float) point.getX(), (float) point.getY(), nodeRadius(node), Color.rgb(211, 47, 47), Color.WHITE, false);
        }

        // 3. Draw Selected Node
        for (CampusGraphNode node : nodes) {
            if (!node.getId().equals(selectedNodeId)) continue;

            MapPoint point = pointFor(node);
            if (point == null) continue;
            drawPin(canvas, (float) point.getX(), (float) point.getY(), nodeRadius(node), Color.BLACK, Color.rgb(255, 235, 59), false);
        }

        // 4. Draw GPS Location
        if (gpsLocation != null && MapCoordinateProjector.isGpsLocationVisibleOnCampusMap(gpsLocation.getLatitude(), gpsLocation.getLongitude())) {
            MapPoint point = MapCoordinateProjector.project(gpsLocation.getLatitude(), gpsLocation.getLongitude(), getWidth(), getHeight());
            if (point != null) {
                drawPin(canvas, (float) point.getX(), (float) point.getY(), dp(8), Color.BLACK, Color.rgb(56, 142, 60), true);
            }
        }
    }

    private void drawPin(Canvas canvas, float x, float y, float radius, int outerColor, int innerColor, boolean isGps) {
        float innerRadius;
        if (isGps) {
            innerRadius = radius * 10f / 11f;
        } else {
            innerRadius = radius * 2f / 3f;
        }

        paint.setColor(outerColor);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(innerColor);
        canvas.drawCircle(x, y, innerRadius, paint);
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
        
        if (nearest != null && nearestDistance <= dp(40)) {
            listener.onNodeTapped(nearest);
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private MapPoint pointFor(CampusGraphNode node) {
        if (node.getLatitude() == null || node.getLongitude() == null) {
            return null;
        }
        return MapCoordinateProjector.project(node.getLatitude(), node.getLongitude(), getWidth(), getHeight());
    }

    private float nodeRadius(CampusGraphNode node) {
        if (node.getNodeType() == NodeType.BUS_STOP) {
            return dp(6.5f);
        }
        if (node.getNodeType() == NodeType.USER_ADDED) {
            return dp(5.5f);
        }
        return dp(5);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
