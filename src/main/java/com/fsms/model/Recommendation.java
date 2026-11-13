package com.fsms.model;

// ============================================================
// Recommendation is the scout's overall verdict on a player.
// Stored in the database as its name (EnumType.STRING).
// ============================================================
public enum Recommendation {

    STRONGLY_RECOMMEND("Strongly recommend", "strong"),
    RECOMMEND("Recommend", "good"),
    MONITOR("Monitor", "neutral"),
    NOT_RECOMMENDED("Do not recommend", "weak");

    private final String displayName;
    private final String cssClass;

    Recommendation(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }

    public String getDisplayName() { return displayName; }
    public String getCssClass() { return cssClass; }
}
