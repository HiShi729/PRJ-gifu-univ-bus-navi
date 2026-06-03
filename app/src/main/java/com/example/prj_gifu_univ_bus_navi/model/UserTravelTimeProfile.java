package com.example.prj_gifu_univ_bus_navi.model;

import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public final class UserTravelTimeProfile {
    private final String calibrationEdgeId;
    private final int standardTravelTimeSeconds;
    private final int measuredTravelTimeSeconds;
    private final double timeScaleFactor;

    public UserTravelTimeProfile(String calibrationEdgeId, int standardTravelTimeSeconds, int measuredTravelTimeSeconds, double timeScaleFactor) {
        this.calibrationEdgeId = calibrationEdgeId;
        this.standardTravelTimeSeconds = standardTravelTimeSeconds;
        this.measuredTravelTimeSeconds = measuredTravelTimeSeconds;
        this.timeScaleFactor = timeScaleFactor;
    }

    public String getCalibrationEdgeId() { return calibrationEdgeId; }
    public int getStandardTravelTimeSeconds() { return standardTravelTimeSeconds; }
    public int getMeasuredTravelTimeSeconds() { return measuredTravelTimeSeconds; }
    public double getTimeScaleFactor() { return timeScaleFactor; }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("calibrationEdgeId", calibrationEdgeId);
            json.put("standardTravelTimeSeconds", standardTravelTimeSeconds);
            json.put("measuredTravelTimeSeconds", measuredTravelTimeSeconds);
            json.put("timeScaleFactor", timeScaleFactor);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static UserTravelTimeProfile fromJson(JSONObject json) {
        return new UserTravelTimeProfile(
            json.optString("calibrationEdgeId", ""),
            json.has("standardTravelTimeSeconds") ? json.optInt("standardTravelTimeSeconds", 60) : json.optInt("standardMinutes", 1) * 60,
            json.has("measuredTravelTimeSeconds") ? json.optInt("measuredTravelTimeSeconds", 60) : json.optInt("measuredMinutes", 1) * 60,
            json.optDouble("timeScaleFactor", 1.0)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTravelTimeProfile)) return false;
        UserTravelTimeProfile that = (UserTravelTimeProfile) o;
        return standardTravelTimeSeconds == that.standardTravelTimeSeconds &&
            measuredTravelTimeSeconds == that.measuredTravelTimeSeconds &&
            Double.compare(that.timeScaleFactor, timeScaleFactor) == 0 &&
            Objects.equals(calibrationEdgeId, that.calibrationEdgeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(calibrationEdgeId, standardTravelTimeSeconds, measuredTravelTimeSeconds, timeScaleFactor);
    }

    @Override
    public String toString() {
        return "UserTravelTimeProfile{" +
            "calibrationEdgeId='" + calibrationEdgeId + '\'' +
            ", standardTravelTimeSeconds=" + standardTravelTimeSeconds +
            ", measuredTravelTimeSeconds=" + measuredTravelTimeSeconds +
            ", timeScaleFactor=" + timeScaleFactor +
            '}';
    }
}
