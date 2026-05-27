package com.example.prj_gifu_univ_bus_navi.model;

import java.util.Objects;

public final class BusStop {
    private final BusStopId id;
    private final String name;
    private final String nodeId;

    public BusStop(BusStopId id, String name, String nodeId) {
        this.id = id;
        this.name = name;
        this.nodeId = nodeId;
    }

    public BusStopId getId() { return id; }
    public String getName() { return name; }
    public String getNodeId() { return nodeId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusStop)) return false;
        BusStop busStop = (BusStop) o;
        return id == busStop.id && Objects.equals(name, busStop.name) && Objects.equals(nodeId, busStop.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, nodeId);
    }

    @Override
    public String toString() {
        return "BusStop{" + "id=" + id + ", name='" + name + '\'' + ", nodeId='" + nodeId + '\'' + '}';
    }
}
