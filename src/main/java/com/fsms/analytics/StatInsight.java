package com.fsms.analytics;

// ============================================================
// StatInsight: how ONE player performs on ONE statistic relative
// to every other player of the same sport stored in the database.
// ============================================================
public final class StatInsight {

    private final StatDefinition definition;
    private final int rawValue;
    private final String formattedValue;
    private final String sportAverageFormatted;
    private final int score;        // 0..100, 100 = best in sport for this stat
    private final int rank;         // 1 = best; 0 when the value is missing
    private final int rankedCount;  // number of players with a real value
    private final String status;    // Above average / Below average / Average / No data

    public StatInsight(StatDefinition definition, int rawValue, String formattedValue,
                       String sportAverageFormatted, int score, int rank, int rankedCount, String status) {
        this.definition = definition;
        this.rawValue = rawValue;
        this.formattedValue = formattedValue;
        this.sportAverageFormatted = sportAverageFormatted;
        this.score = score;
        this.rank = rank;
        this.rankedCount = rankedCount;
        this.status = status;
    }

    public StatDefinition getDefinition() { return definition; }
    public int getRawValue() { return rawValue; }
    public String getFormattedValue() { return formattedValue; }
    public String getSportAverageFormatted() { return sportAverageFormatted; }
    public int getScore() { return score; }
    public int getRank() { return rank; }
    public int getRankedCount() { return rankedCount; }
    public String getStatus() { return status; }
    public boolean isMissing() { return rank == 0; }

    // CSS modifier used by the templates
    public String getStatusClass() {
        switch (status) {
            case "Above average": return "good";
            case "Below average": return "weak";
            case "No data":       return "muted";
            default:              return "neutral";
        }
    }
}
