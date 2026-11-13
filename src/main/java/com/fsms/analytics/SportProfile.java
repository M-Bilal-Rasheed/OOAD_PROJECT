package com.fsms.analytics;

import com.fsms.model.PlayerStatistics;

import java.util.List;

// ============================================================
// Abstraction + Polymorphism: SportProfile is an abstract class.
// Each sport (Football, Cricket, Basketball, Tennis) provides its
// own subclass describing its four statistics and its own derived
// KPIs. PerformanceAnalyticsService works only with this abstract
// type, so adding a new sport needs no change in the service.
// ============================================================
public abstract class SportProfile {

    // Name of the sport exactly as stored in Player.sport
    public abstract String getSport();

    // The four statistic definitions, in slot order 1..4
    public abstract List<StatDefinition> getStatDefinitions();

    // Sport-specific KPIs calculated from the stored statistics
    public abstract List<DerivedMetric> derivedMetrics(PlayerStatistics stats);

    // Short explanation shown on the analytics page
    public abstract String getDescription();

    // Shared helper — safe division used by subclasses
    protected static double ratio(int numerator, int denominator) {
        return denominator == 0 ? 0.0 : (double) numerator / denominator;
    }

    // Shared helper: the label of each stat in order (used for parity with Player labels)
    public String[] getLabels() {
        List<StatDefinition> defs = getStatDefinitions();
        String[] labels = new String[defs.size()];
        for (int i = 0; i < defs.size(); i++) {
            labels[i] = defs.get(i).getLabel();
        }
        return labels;
    }
}
