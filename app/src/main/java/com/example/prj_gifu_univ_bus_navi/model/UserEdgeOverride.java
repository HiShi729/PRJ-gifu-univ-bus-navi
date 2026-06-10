package com.example.prj_gifu_univ_bus_navi.model;

import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public final class UserEdgeOverride {
    private final String baseEdgeId;
    private final int travelTimeSeconds;

    public UserEdgeOverride(String baseEdgeId, int travelTimeSeconds) {
        this.baseEdgeId = baseEdgeId;
        this.travelTimeSeconds = travelTimeSeconds;
    }

    public String getBaseEdgeId() { return baseEdgeId; }
    public int getTravelTimeSeconds() { return travelTimeSeconds; }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("baseEdgeId", baseEdgeId);
            json.put("travelTimeSeconds", travelTimeSeconds);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static UserEdgeOverride fromJson(JSONObject json) {
        int seconds = json.has("travelTimeSeconds")
            ? json.optInt("travelTimeSeconds", 60)
            : json.optInt("minutes", 1) * 60;
        return new UserEdgeOverride(json.optString("baseEdgeId", ""), seconds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEdgeOverride)) return false;
        UserEdgeOverride that = (UserEdgeOverride) o;
        return travelTimeSeconds == that.travelTimeSeconds && Objects.equals(baseEdgeId, that.baseEdgeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseEdgeId, travelTimeSeconds);
    }

    @Override
    public String toString() {
        return "UserEdgeOverride{" + "baseEdgeId='" + baseEdgeId + '\'' + ", travelTimeSeconds=" + travelTimeSeconds + '}';
    }
}
