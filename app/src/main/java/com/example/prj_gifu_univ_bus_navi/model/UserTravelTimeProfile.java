package com.example.prj_gifu_univ_bus_navi.model;

import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public final class UserTravelTimeProfile {
    private final String calibrationEdgeId;
    private final int standardMinutes;
    private final int measuredMinutes;
    private final double timeScaleFactor;

    public UserTravelTimeProfile(String calibrationEdgeId, int standardMinutes, int measuredMinutes, double timeScaleFactor) {
        this.calibrationEdgeId = calibrationEdgeId;
        this.standardMinutes = standardMinutes;
        this.measuredMinutes = measuredMinutes;
        this.timeScaleFactor = timeScaleFactor;
    }

    public String getCalibrationEdgeId() { return calibrationEdgeId; }
    public int getStandardMinutes() { return standardMinutes; }
    public int getMeasuredMinutes() { return measuredMinutes; }
    public double getTimeScaleFactor() { return timeScaleFactor; }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("calibrationEdgeId", calibrationEdgeId);
            json.put("standardMinutes", standardMinutes);
            json.put("measuredMinutes", measuredMinutes);
            json.put("timeScaleFactor", timeScaleFactor);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static UserTravelTimeProfile fromJson(JSONObject json) {
        return new UserTravelTimeProfile(
            json.optString("calibrationEdgeId", ""),
            json.optInt("standardMinutes", 1),
            json.optInt("measuredMinutes", 1),
            json.optDouble("timeScaleFactor", 1.0)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTravelTimeProfile)) return false;
        UserTravelTimeProfile that = (UserTravelTimeProfile) o;
        return standardMinutes == that.standardMinutes &&
            measuredMinutes == that.measuredMinutes &&
            Double.compare(that.timeScaleFactor, timeScaleFactor) == 0 &&
            Objects.equals(calibrationEdgeId, that.calibrationEdgeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(calibrationEdgeId, standardMinutes, measuredMinutes, timeScaleFactor);
    }

    @Override
    public String toString() {
        return "UserTravelTimeProfile{" +
            "calibrationEdgeId='" + calibrationEdgeId + '\'' +
            ", standardMinutes=" + standardMinutes +
            ", measuredMinutes=" + measuredMinutes +
            ", timeScaleFactor=" + timeScaleFactor +
            '}';
    }
}
