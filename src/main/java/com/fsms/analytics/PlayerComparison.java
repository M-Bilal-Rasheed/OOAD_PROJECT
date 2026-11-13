package com.fsms.analytics;

import com.fsms.model.Player;

import java.util.List;

// ============================================================
// PlayerComparison: result of comparing two players of the SAME
// sport — one ComparisonRow per stat, win counts, both performance
// indexes and a radar chart of the normalised scores.
// ============================================================
public final class PlayerComparison {

    private final Player playerA;
    private final Player playerB;
    private final SportProfile profile;
    private final List<ComparisonRow> rows;
    private final int winsA;
    private final int winsB;
    private final int ties;
    private final int indexA;
    private final int indexB;
    private final RadarChart radar;

    public PlayerComparison(Player playerA, Player playerB, SportProfile profile, List<ComparisonRow> rows,
                            int winsA, int winsB, int ties, int indexA, int indexB, RadarChart radar) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.profile = profile;
        this.rows = rows;
        this.winsA = winsA;
        this.winsB = winsB;
        this.ties = ties;
        this.indexA = indexA;
        this.indexB = indexB;
        this.radar = radar;
    }

    public Player getPlayerA() { return playerA; }
    public Player getPlayerB() { return playerB; }
    public SportProfile getProfile() { return profile; }
    public List<ComparisonRow> getRows() { return rows; }
    public int getWinsA() { return winsA; }
    public int getWinsB() { return winsB; }
    public int getTies() { return ties; }
    public int getIndexA() { return indexA; }
    public int getIndexB() { return indexB; }
    public RadarChart getRadar() { return radar; }

    // Short verdict based on how many statistics each player wins
    public String getVerdict() {
        if (winsA > winsB) return playerA.getName() + " leads on " + winsA + " of " + rows.size() + " statistics";
        if (winsB > winsA) return playerB.getName() + " leads on " + winsB + " of " + rows.size() + " statistics";
        return "Evenly matched across " + rows.size() + " statistics";
    }
}
