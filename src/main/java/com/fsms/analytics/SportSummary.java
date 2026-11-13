package com.fsms.analytics;

import java.util.List;

// ============================================================
// SportSummary: everything the Analytics page shows for a sport —
// KPI figures, per-stat summaries and the overall leaderboard.
// ============================================================
public final class SportSummary {

    private final SportProfile profile;
    private final int playerCount;
    private final String averageAge;
    private final long valuedCount;
    private final List<StatSummary> statSummaries;
    private final List<RankedEntry> indexLeaderboard;

    public SportSummary(SportProfile profile, int playerCount, String averageAge, long valuedCount,
                        List<StatSummary> statSummaries, List<RankedEntry> indexLeaderboard) {
        this.profile = profile;
        this.playerCount = playerCount;
        this.averageAge = averageAge;
        this.valuedCount = valuedCount;
        this.statSummaries = statSummaries;
        this.indexLeaderboard = indexLeaderboard;
    }

    public SportProfile getProfile() { return profile; }
    public String getSport() { return profile.getSport(); }
    public int getPlayerCount() { return playerCount; }
    public String getAverageAge() { return averageAge; }
    public long getValuedCount() { return valuedCount; }
    public List<StatSummary> getStatSummaries() { return statSummaries; }
    public List<RankedEntry> getIndexLeaderboard() { return indexLeaderboard; }
    public boolean isEmpty() { return playerCount == 0; }
    public RankedEntry getTopPerformer() {
        return indexLeaderboard.isEmpty() ? null : indexLeaderboard.get(0);
    }
}
