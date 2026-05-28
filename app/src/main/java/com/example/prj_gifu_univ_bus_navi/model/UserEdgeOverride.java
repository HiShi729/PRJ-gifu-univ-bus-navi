package com.example.prj_gifu_univ_bus_navi.model;

import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public final class UserEdgeOverride {
    private final String baseEdgeId;
    private final int minutes;

    public UserEdgeOverride(String baseEdgeId, int minutes) {
        this.baseEdgeId = baseEdgeId;
        this.minutes = minutes;
    }

    public String getBaseEdgeId() { return baseEdgeId; }
    public int getMinutes() { return minutes; }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("baseEdgeId", baseEdgeId);
            json.put("minutes", minutes);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static UserEdgeOverride fromJson(JSONObject json) {
        return new UserEdgeOverride(json.optString("baseEdgeId", ""), json.optInt("minutes", 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEdgeOverride)) return false;
        UserEdgeOverride that = (UserEdgeOverride) o;
        return minutes == that.minutes && Objects.equals(baseEdgeId, that.baseEdgeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseEdgeId, minutes);
    }

    @Override
    public String toString() {
        return "UserEdgeOverride{" + "baseEdgeId='" + baseEdgeId + '\'' + ", minutes=" + minutes + '}';
    }
}
