package com.example.prj_gifu_univ_bus_navi.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ShortestPathResult {
    private final int totalMinutes;
    private final List<String> nodePath;

    public ShortestPathResult(int totalMinutes, List<String> nodePath) {
        this.totalMinutes = totalMinutes;
        this.nodePath = Collections.unmodifiableList(new ArrayList<>(nodePath));
    }

    public int getTotalMinutes() { return totalMinutes; }
    public List<String> getNodePath() { return nodePath; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortestPathResult)) return false;
        ShortestPathResult that = (ShortestPathResult) o;
        return totalMinutes == that.totalMinutes && Objects.equals(nodePath, that.nodePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalMinutes, nodePath);
    }

    @Override
    public String toString() {
        return "ShortestPathResult{" + "totalMinutes=" + totalMinutes + ", nodePath=" + nodePath + '}';
    }
}
