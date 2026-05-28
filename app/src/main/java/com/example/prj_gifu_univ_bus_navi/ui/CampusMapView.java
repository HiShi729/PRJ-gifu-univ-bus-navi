package com.example.prj_gifu_univ_bus_navi.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.prj_gifu_univ_bus_navi.R;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode;
import com.example.prj_gifu_univ_bus_navi.model.NodeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CampusMapView extends View {
    public interface OnNodeTapListener {
        void onNodeTapped(CampusGraphNode node);
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<CampusGraphNode> nodes = new ArrayList<>();
    private Location gpsLocation;
    private OnNodeTapListener listener;
    private Bitmap backgroundBitmap;
    private String selectedNodeId;
    private final List<RectF> placedLabelRects = new ArrayList<>();

    public CampusMapView(Context context) {
        super(context);
        init();
    }

    public CampusMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setColor(Color.rgb(38, 50, 56));
        textPaint.setTextSize(sp(11));
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
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
        List<LabelRequest> labelRequests = new ArrayList<>();
        placedLabelRects.clear();

        // Prepare Node Labels
        for (CampusGraphNode node : nodes) {
            MapPoint point = pointFor(node);
            if (point == null) continue;

            float radius = nodeRadius(node);
            boolean isSelected = node.getId().equals(selectedNodeId);
            boolean isBusStop = node.getNodeType() == NodeType.BUS_STOP;

            labelRequests.add(new LabelRequest(
                node.getName(),
                (float) point.getX(),
                (float) point.getY(),
                radius,
                node.getNodeType(),
                isSelected || isBusStop,
                isSelected
            ));
        }

        // Prepare GPS Label
        if (gpsLocation != null && MapCoordinateProjector.isGpsLocationVisibleOnCampusMap(gpsLocation.getLatitude(), gpsLocation.getLongitude())) {
            MapPoint point = MapCoordinateProjector.project(gpsLocation.getLatitude(), gpsLocation.getLongitude(), getWidth(), getHeight());
            if (point != null) {
                labelRequests.add(new LabelRequest(
                    "現在地",
                    (float) point.getX(),
                    (float) point.getY(),
                    dp(10),
                    null,
                    true,
                    false
                ));
            }
        }

        // Sort by priority: HighPriority first, then NodeType, then selected
        Collections.sort(labelRequests, (a, b) -> {
            if (a.isHighPriority != b.isHighPriority) return a.isHighPriority ? -1 : 1;
            if (a.nodeType != b.nodeType) {
                if (a.nodeType == NodeType.BUS_STOP) return -1;
                if (b.nodeType == NodeType.BUS_STOP) return 1;
            }
            if (a.isSelected != b.isSelected) return a.isSelected ? -1 : 1;
            return 0;
        });

        // First pass: decide label positions
        for (LabelRequest req : labelRequests) {
            req.finalRect = findBestLabelBounds(req);
            if (req.finalRect != null) {
                placedLabelRects.add(req.finalRect);
            }
        }

        // Draw Pins
        paint.setStyle(Paint.Style.FILL);
        for (CampusGraphNode node : nodes) {
            MapPoint point = pointFor(node);
            if (point == null) continue;
            drawPin(canvas, (float) point.getX(), (float) point.getY(), nodeRadius(node), node.getNodeType(), node.getId().equals(selectedNodeId));
        }

        if (gpsLocation != null && MapCoordinateProjector.isGpsLocationVisibleOnCampusMap(gpsLocation.getLatitude(), gpsLocation.getLongitude())) {
            MapPoint point = MapCoordinateProjector.project(gpsLocation.getLatitude(), gpsLocation.getLongitude(), getWidth(), getHeight());
            if (point != null) {
                drawGpsPin(canvas, (float) point.getX(), (float) point.getY(), dp(10));
            }
        }

        // Draw Labels
        for (LabelRequest req : labelRequests) {
            if (req.finalRect != null) {
                drawPlacedLabel(canvas, req);
            }
        }
    }

    private void drawPin(Canvas canvas, float x, float y, float radius, NodeType nodeType, boolean isSelected) {
        if (nodeType == NodeType.BUS_STOP) {
            paint.setColor(Color.rgb(211, 47, 47));
        } else if (nodeType == NodeType.USER_ADDED) {
            paint.setColor(Color.rgb(25, 118, 210));
        } else {
            paint.setColor(Color.rgb(69, 90, 100));
        }
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(isSelected ? Color.rgb(255, 235, 59) : Color.WHITE);
        canvas.drawCircle(x, y, Math.max(dp(4), radius * 0.35f), paint);
    }

    private void drawGpsPin(Canvas canvas, float x, float y, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(56, 142, 60));
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, dp(4), paint);
    }

    private RectF findBestLabelBounds(LabelRequest req) {
        if (req.text == null || req.text.isEmpty()) return null;

        float paddingX = dp(6);
        float paddingY = dp(3);
        float textWidth = textPaint.measureText(req.text);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float labelWidth = textWidth + paddingX * 2;
        float labelHeight = metrics.descent - metrics.ascent + paddingY * 2;

        float bestOverlap = Float.MAX_VALUE;
        RectF bestRect = null;

        // 8 Candidate positions
        float distance = req.radius + dp(4);
        float[][] offsets = {
            {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}
        };

        for (float[] offset : offsets) {
            float centerX = req.x + offset[0] * (distance + labelWidth / 2);
            float centerY = req.y + offset[1] * (distance + labelHeight / 2);
            
            // Adjust to align edge rather than center if using unit vectors
            float left, top;
            if (offset[0] == 0) left = req.x - labelWidth / 2;
            else if (offset[0] > 0) left = req.x + distance;
            else left = req.x - distance - labelWidth;

            if (offset[1] == 0) top = req.y - labelHeight / 2;
            else if (offset[1] > 0) top = req.y + distance;
            else top = req.y - distance - labelHeight;

            RectF candidate = new RectF(left, top, left + labelWidth, top + labelHeight);

            // Check if within view bounds
            if (candidate.left < dp(2) || candidate.right > getWidth() - dp(2) ||
                candidate.top < dp(2) || candidate.bottom > getHeight() - dp(2)) {
                continue;
            }

            float overlap = calculateOverlapArea(candidate);
            if (overlap == 0) return candidate; // Found perfect spot

            if (overlap < bestOverlap) {
                bestOverlap = overlap;
                bestRect = candidate;
            }
        }

        if (req.isHighPriority) {
            return bestRect != null ? bestRect : new RectF(req.x + distance, req.y - labelHeight / 2, req.x + distance + labelWidth, req.y + labelHeight / 2);
        } else {
            // For normal nodes, only show if overlap is small
            if (bestRect != null && bestOverlap < (labelWidth * labelHeight * 0.2f)) {
                return bestRect;
            }
            return null;
        }
    }

    private float calculateOverlapArea(RectF candidate) {
        float totalOverlap = 0;
        for (RectF placed : placedLabelRects) {
            if (RectF.intersects(candidate, placed)) {
                float left = Math.max(candidate.left, placed.left);
                float top = Math.max(candidate.top, placed.top);
                float right = Math.min(candidate.right, placed.right);
                float bottom = Math.min(candidate.bottom, placed.bottom);
                totalOverlap += (right - left) * (bottom - top);
            }
        }
        return totalOverlap;
    }

    private void drawPlacedLabel(Canvas canvas, LabelRequest req) {
        paint.setStyle(Paint.Style.FILL);
        if (req.nodeType == NodeType.BUS_STOP) {
            paint.setColor(Color.argb(235, 255, 235, 238));
        } else if (req.isSelected) {
            paint.setColor(Color.argb(235, 255, 253, 231));
        } else if (req.nodeType == null) { // GPS
            paint.setColor(Color.argb(235, 232, 245, 233));
        } else {
            paint.setColor(Color.argb(220, 255, 255, 255));
        }
        canvas.drawRoundRect(req.finalRect, dp(4), dp(4), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        if (req.nodeType == NodeType.BUS_STOP) {
            paint.setColor(Color.rgb(229, 115, 115));
        } else if (req.isSelected) {
            paint.setColor(Color.rgb(251, 192, 45));
        } else {
            paint.setColor(Color.rgb(207, 216, 220));
        }
        canvas.drawRoundRect(req.finalRect, dp(4), dp(4), paint);

        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float paddingX = dp(6);
        canvas.drawText(req.text, req.finalRect.left + paddingX, req.finalRect.top + dp(3) - metrics.ascent, textPaint);
    }

    private static class LabelRequest {
        String text;
        float x, y;
        float radius;
        NodeType nodeType;
        boolean isHighPriority;
        boolean isSelected;
        RectF finalRect;

        LabelRequest(String text, float x, float y, float radius, NodeType nodeType, boolean isHighPriority, boolean isSelected) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.nodeType = nodeType;
            this.isHighPriority = isHighPriority;
            this.isSelected = isSelected;
        }
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
        
        // Also check labels for tap
        if (nearestDistance > dp(25)) {
            for (RectF rect : placedLabelRects) {
                if (rect.contains(event.getX(), event.getY())) {
                    float centerX = rect.centerX();
                    float centerY = rect.centerY();
                    for (CampusGraphNode node : nodes) {
                        MapPoint point = pointFor(node);
                        if (point == null) continue;
                        double distance = Math.hypot(point.getX() - centerX, point.getY() - centerY);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearest = node;
                        }
                    }
                    break;
                }
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
            return dp(13);
        }
        if (node.getNodeType() == NodeType.USER_ADDED) {
            return dp(11);
        }
        return dp(10);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
