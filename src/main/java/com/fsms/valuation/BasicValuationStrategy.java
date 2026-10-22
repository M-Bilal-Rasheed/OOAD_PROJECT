package com.fsms.valuation;

import com.fsms.model.Player;
import com.fsms.model.PlayerStatistics;
import org.springframework.stereotype.Component;

// ============================================================
// Polymorphism: BasicValuationStrategy IMPLEMENTS ValuationStrategy.
// PlayerService calls calculateValue() through the ValuationStrategy
// interface reference — one interface, this implementation. Polymorphism.
// ============================================================
@Component
public class BasicValuationStrategy implements ValuationStrategy {  // Polymorphism

    // ============================================================
    // IMPORTANT: All formulas below are EDUCATIONAL ESTIMATES only.
    // They are NOT real market valuations. Weights are simplified
    // for demonstration purposes.
    // ============================================================

    // ---- Football weights ----
    private static final long FB_STAT1 = 100_000L;  // per goal
    private static final long FB_STAT2 =  50_000L;  // per assist
    private static final long FB_STAT3 =  10_000L;  // per appearance
    private static final long FB_STAT4 =  20_000L;  // per speed rating point

    // ---- Cricket weights ----
    private static final long CR_STAT1 =   5_000L;  // per run
    private static final long CR_STAT2 = 200_000L;  // per wicket
    private static final long CR_STAT3 =  15_000L;  // per batting average ×10
    private static final long CR_STAT4 =  10_000L;  // per economy rate ×10 (lower better, inverted below)

    // ---- Basketball weights ----
    private static final long BB_STAT1 =  80_000L;  // per point
    private static final long BB_STAT2 =  40_000L;  // per assist
    private static final long BB_STAT3 =  30_000L;  // per rebound
    private static final long BB_STAT4 =  25_000L;  // per shooting % ×10

    // ---- Tennis weights ----
    private static final long TN_STAT1 = 150_000L;  // per win
    private static final long TN_STAT2 =  50_000L;  // per ranking point (inverted: 200-rank)
    private static final long TN_STAT3 =  10_000L;  // per ace
    private static final long TN_STAT4 =  20_000L;  // per 1st serve % ×10

    // ============================================================
    // Polymorphism: @Override fulfils the calculateValue() contract
    // defined in the ValuationStrategy interface.
    // Strategy Design Pattern: this method is called via the interface
    // reference in PlayerService — swappable without changing service code.
    // ============================================================
    @Override  // Polymorphism
    public long calculateValue(Player player) {

        PlayerStatistics stats = player.getStatistics();  // Composition — accessing owned object
        String sport = player.getSport();
        long value = 0;

        if (sport == null) {
            sport = "Football";  // safe default
        }

        switch (sport) {
            case "Football":
                // Goals × 100k + Assists × 50k + Appearances × 10k + Speed × 20k
                value = (stats.getStat1() * FB_STAT1)
                      + (stats.getStat2() * FB_STAT2)
                      + (stats.getStat3() * FB_STAT3)
                      + (stats.getStat4() * FB_STAT4);
                break;

            case "Cricket":
                // Runs × 5k + Wickets × 200k + BatAvg×10 × 15k + (100 - EconRate×10) × 10k
                int econPenalty = Math.max(0, 100 - stats.getStat4());
                value = (stats.getStat1() * CR_STAT1)
                      + (stats.getStat2() * CR_STAT2)
                      + (stats.getStat3() * CR_STAT3)
                      + (econPenalty     * CR_STAT4);
                break;

            case "Basketball":
                // Points × 80k + Assists × 40k + Rebounds × 30k + Shooting%×10 × 25k
                value = (stats.getStat1() * BB_STAT1)
                      + (stats.getStat2() * BB_STAT2)
                      + (stats.getStat3() * BB_STAT3)
                      + (stats.getStat4() * BB_STAT4);
                break;

            case "Tennis":
                // Wins × 150k + (200-Ranking) × 50k + Aces × 10k + 1stServe%×10 × 20k
                int rankingScore = Math.max(0, 200 - stats.getStat2());
                value = (stats.getStat1() * TN_STAT1)
                      + (rankingScore     * TN_STAT2)
                      + (stats.getStat3() * TN_STAT3)
                      + (stats.getStat4() * TN_STAT4);
                break;

            default:
                // Generic fallback formula
                value = (stats.getStat1() * 50_000L)
                      + (stats.getStat2() * 30_000L)
                      + (stats.getStat3() * 20_000L)
                      + (stats.getStat4() * 10_000L);
                break;
        }

        return value;
    }
}
