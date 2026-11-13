package com.fsms.analytics;

import java.util.List;

// ============================================================
// StatSummary: sport-wide summary for ONE statistic — average,
// best value and a top-5 leaderboard ordered by the stat's own
// rule (e.g. lowest economy rate first in Cricket).
// ============================================================
public final class StatSummary {

    private final StatDefinition definition;
    private final String averageFormatted;
    private final RankedEntry best;
    private final List<RankedEntry> leaders;
    private final int rankedCount;

    public StatSummary(StatDefinition definition, String averageFormatted, RankedEntry best,
                       List<RankedEntry> leaders, int rankedCount) {
        this.definition = definition;
        this.averageFormatted = averageFormatted;
        this.best = best;
        this.leaders = leaders;
        this.rankedCount = rankedCount;
    }

    public StatDefinition getDefinition() { return definition; }
    public String getAverageFormatted() { return averageFormatted; }
    public RankedEntry getBest() { return best; }
    public List<RankedEntry> getLeaders() { return leaders; }
    public int getRankedCount() { return rankedCount; }
}
