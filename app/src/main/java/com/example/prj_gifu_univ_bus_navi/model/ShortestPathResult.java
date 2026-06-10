package com.example.prj_gifu_univ_bus_navi.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ShortestPathResult {
    private final int totalSeconds;
    private final List<String> nodePath;

    public ShortestPathResult(int totalSeconds, List<String> nodePath) {
        this.totalSeconds = totalSeconds;
        this.nodePath = Collections.unmodifiableList(new ArrayList<>(nodePath));
    }

    public int getTotalSeconds() { return totalSeconds; }
    public List<String> getNodePath() { return nodePath; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortestPathResult)) return false;
        ShortestPathResult that = (ShortestPathResult) o;
        return totalSeconds == that.totalSeconds && Objects.equals(nodePath, that.nodePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalSeconds, nodePath);
    }

    @Override
    public String toString() {
        return "ShortestPathResult{" + "totalSeconds=" + totalSeconds + ", nodePath=" + nodePath + '}';
    }
}
