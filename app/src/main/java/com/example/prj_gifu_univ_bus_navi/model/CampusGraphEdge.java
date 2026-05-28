package com.example.prj_gifu_univ_bus_navi.model;

import java.util.Objects;

public final class CampusGraphEdge {
    private final String id;
    private final String fromNodeId;
    private final String toNodeId;
    private final int minutes;
    private final boolean bidirectional;
    private final EdgeSourceType sourceType;
    private final boolean selectableForUserEdit;

    public CampusGraphEdge(String id, String fromNodeId, String toNodeId, int minutes, boolean bidirectional, EdgeSourceType sourceType, boolean selectableForUserEdit) {
        this.id = id;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.minutes = minutes;
        this.bidirectional = bidirectional;
        this.sourceType = sourceType;
        this.selectableForUserEdit = selectableForUserEdit;
    }

    public String getId() { return id; }
    public String getFromNodeId() { return fromNodeId; }
    public String getToNodeId() { return toNodeId; }
    public int getMinutes() { return minutes; }
    public boolean isBidirectional() { return bidirectional; }
    public EdgeSourceType getSourceType() { return sourceType; }
    public boolean isSelectableForUserEdit() { return selectableForUserEdit; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CampusGraphEdge)) return false;
        CampusGraphEdge that = (CampusGraphEdge) o;
        return minutes == that.minutes &&
            bidirectional == that.bidirectional &&
            selectableForUserEdit == that.selectableForUserEdit &&
            Objects.equals(id, that.id) &&
            Objects.equals(fromNodeId, that.fromNodeId) &&
            Objects.equals(toNodeId, that.toNodeId) &&
            sourceType == that.sourceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromNodeId, toNodeId, minutes, bidirectional, sourceType, selectableForUserEdit);
    }

    @Override
    public String toString() {
        return "CampusGraphEdge{" +
            "id='" + id + '\'' +
            ", fromNodeId='" + fromNodeId + '\'' +
            ", toNodeId='" + toNodeId + '\'' +
            ", minutes=" + minutes +
            ", bidirectional=" + bidirectional +
            ", sourceType=" + sourceType +
            ", selectableForUserEdit=" + selectableForUserEdit +
            '}';
    }
}
