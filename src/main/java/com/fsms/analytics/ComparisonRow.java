package com.fsms.analytics;

// ============================================================
// ComparisonRow: one statistic compared between two players of the
// same sport. The winner is decided by StatDefinition.compareRaw(),
// so "lower is better" stats (economy, ranking) are handled correctly.
// ============================================================
public final class ComparisonRow {

    public static final String A = "A";
    public static final String B = "B";
    public static final String TIE = "TIE";

    private final StatDefinition definition;
    private final String formattedA;
    private final String formattedB;
    private final int scoreA;
    private final int scoreB;
    private final String winner;

    public ComparisonRow(StatDefinition definition, String formattedA, String formattedB,
                         int scoreA, int scoreB, String winner) {
        this.definition = definition;
        this.formattedA = formattedA;
        this.formattedB = formattedB;
        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.winner = winner;
    }

    public StatDefinition getDefinition() { return definition; }
    public String getFormattedA() { return formattedA; }
    public String getFormattedB() { return formattedB; }
    public int getScoreA() { return scoreA; }
    public int getScoreB() { return scoreB; }
    public int getBarA() { return Math.max(3, scoreA); }
    public int getBarB() { return Math.max(3, scoreB); }
    public String getWinner() { return winner; }
    public boolean isAWins() { return A.equals(winner); }
    public boolean isBWins() { return B.equals(winner); }
    public boolean isTie() { return TIE.equals(winner); }
}
