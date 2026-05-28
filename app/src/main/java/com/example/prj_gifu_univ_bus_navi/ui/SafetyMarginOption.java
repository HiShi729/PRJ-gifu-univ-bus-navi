package com.example.prj_gifu_univ_bus_navi.ui;

public final class SafetyMarginOption {
    private final String label;
    private final int minutes;

    public SafetyMarginOption(String label, int minutes) {
        this.label = label;
        this.minutes = minutes;
    }

    public String getLabel() { return label; }
    public int getMinutes() { return minutes; }

    @Override
    public String toString() {
        return label;
    }
}
