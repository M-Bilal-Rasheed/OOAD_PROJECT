package com.fsms.analytics;

// ============================================================
// DerivedMetric is a KPI calculated from a player's stored
// statistics (e.g. goal involvements per appearance).
// It never uses external or invented data.
// ============================================================
public final class DerivedMetric {

    private final String label;
    private final String value;
    private final String description;

    public DerivedMetric(String label, String value, String description) {
        this.label = label;
        this.value = value;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getValue() { return value; }
    public String getDescription() { return description; }
}
