package com.example.prj_gifu_univ_bus_navi.model;

import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public final class UserGraphNodeInput {
    private final String name;
    private final String connectedNodeId;
    private final int minutesToConnectedNode;
    private final boolean selectableAsStart;
    private final Double latitude;
    private final Double longitude;
    private final UserNodeCoordinateSource coordinateSource;

    public UserGraphNodeInput(String name, String connectedNodeId, int minutesToConnectedNode, boolean selectableAsStart) {
        this(name, connectedNodeId, minutesToConnectedNode, selectableAsStart, null, null, UserNodeCoordinateSource.NONE);
    }

    public UserGraphNodeInput(
        String name,
        String connectedNodeId,
        int minutesToConnectedNode,
        boolean selectableAsStart,
        Double latitude,
        Double longitude,
        UserNodeCoordinateSource coordinateSource
    ) {
        this.name = name;
        this.connectedNodeId = connectedNodeId;
        this.minutesToConnectedNode = minutesToConnectedNode;
        this.selectableAsStart = selectableAsStart;
        this.latitude = latitude;
        this.longitude = longitude;
        this.coordinateSource = coordinateSource == null ? UserNodeCoordinateSource.NONE : coordinateSource;
    }

    public String getName() { return name; }
    public String getConnectedNodeId() { return connectedNodeId; }
    public int getMinutesToConnectedNode() { return minutesToConnectedNode; }
    public boolean isSelectableAsStart() { return selectableAsStart; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public UserNodeCoordinateSource getCoordinateSource() { return coordinateSource; }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        put(json, "name", name);
        put(json, "connectedNodeId", connectedNodeId);
        put(json, "minutesToConnectedNode", minutesToConnectedNode);
        put(json, "isSelectableAsStart", selectableAsStart);
        if (latitude != null) put(json, "latitude", latitude);
        if (longitude != null) put(json, "longitude", longitude);
        put(json, "coordinateSource", coordinateSource.name());
        return json;
    }

    public static UserGraphNodeInput fromJson(JSONObject json) {
        String source = json.optString("coordinateSource", UserNodeCoordinateSource.NONE.name());
        return new UserGraphNodeInput(
            json.optString("name", ""),
            json.optString("connectedNodeId", ""),
            json.optInt("minutesToConnectedNode", 1),
            json.optBoolean("isSelectableAsStart", true),
            json.has("latitude") && !json.isNull("latitude") ? json.optDouble("latitude") : null,
            json.has("longitude") && !json.isNull("longitude") ? json.optDouble("longitude") : null,
            parseCoordinateSource(source)
        );
    }

    private static UserNodeCoordinateSource parseCoordinateSource(String value) {
        try {
            return UserNodeCoordinateSource.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return UserNodeCoordinateSource.NONE;
        }
    }

    private static void put(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException ignored) {
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGraphNodeInput)) return false;
        UserGraphNodeInput that = (UserGraphNodeInput) o;
        return minutesToConnectedNode == that.minutesToConnectedNode &&
            selectableAsStart == that.selectableAsStart &&
            Objects.equals(name, that.name) &&
            Objects.equals(connectedNodeId, that.connectedNodeId) &&
            Objects.equals(latitude, that.latitude) &&
            Objects.equals(longitude, that.longitude) &&
            coordinateSource == that.coordinateSource;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, connectedNodeId, minutesToConnectedNode, selectableAsStart, latitude, longitude, coordinateSource);
    }

    @Override
    public String toString() {
        return "UserGraphNodeInput{" +
            "name='" + name + '\'' +
            ", connectedNodeId='" + connectedNodeId + '\'' +
            ", minutesToConnectedNode=" + minutesToConnectedNode +
            ", selectableAsStart=" + selectableAsStart +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", coordinateSource=" + coordinateSource +
            '}';
    }
}
