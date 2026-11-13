package com.fsms.analytics;

import com.fsms.model.Player;

import java.util.List;

// ============================================================
// PlayerInsight: the full analytics picture for one player —
// per-stat insights, derived KPIs, a 0..100 performance index
// (average of the per-stat scores within the same sport), and the
// player's strongest and weakest statistic.
// ============================================================
public final class PlayerInsight {

    private final Player player;
    private final SportProfile profile;
    private final List<StatInsight> stats;
    private final List<DerivedMetric> derivedMetrics;
    private final int performanceIndex;
    private final int peerCount;
    private final StatInsight strongest;
    private final StatInsight weakest;

    public PlayerInsight(Player player, SportProfile profile, List<StatInsight> stats,
                         List<DerivedMetric> derivedMetrics, int performanceIndex, int peerCount,
                         StatInsight strongest, StatInsight weakest) {
        this.player = player;
        this.profile = profile;
        this.stats = stats;
        this.derivedMetrics = derivedMetrics;
        this.performanceIndex = performanceIndex;
        this.peerCount = peerCount;
        this.strongest = strongest;
        this.weakest = weakest;
    }

    public Player getPlayer() { return player; }
    public SportProfile getProfile() { return profile; }
    public List<StatInsight> getStats() { return stats; }
    public List<DerivedMetric> getDerivedMetrics() { return derivedMetrics; }
    public int getPerformanceIndex() { return performanceIndex; }
    public int getPeerCount() { return peerCount; }
    public StatInsight getStrongest() { return strongest; }
    public StatInsight getWeakest() { return weakest; }
}
