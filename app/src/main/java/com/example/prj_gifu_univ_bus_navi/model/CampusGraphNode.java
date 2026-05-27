package com.example.prj_gifu_univ_bus_navi.model;

import java.util.Objects;

public final class CampusGraphNode {
    private final String id;
    private final String name;
    private final NodeType nodeType;
    private final boolean selectableAsStart;
    private final Double latitude;
    private final Double longitude;

    public CampusGraphNode(String id, String name, NodeType nodeType, boolean selectableAsStart, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.nodeType = nodeType;
        this.selectableAsStart = selectableAsStart;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public NodeType getNodeType() { return nodeType; }
    public boolean isSelectableAsStart() { return selectableAsStart; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CampusGraphNode)) return false;
        CampusGraphNode that = (CampusGraphNode) o;
        return selectableAsStart == that.selectableAsStart &&
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            nodeType == that.nodeType &&
            Objects.equals(latitude, that.latitude) &&
            Objects.equals(longitude, that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, nodeType, selectableAsStart, latitude, longitude);
    }

    @Override
    public String toString() {
        return "CampusGraphNode{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", nodeType=" + nodeType +
            ", selectableAsStart=" + selectableAsStart +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            '}';
    }
}
