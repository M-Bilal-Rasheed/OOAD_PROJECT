package com.fsms.analytics;

import com.fsms.model.PlayerStatistics;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

// Inheritance: TennisProfile EXTENDS SportProfile.
// Ranking: LOWER is better, and 0 means the player has no ranking.
// 1st Serve % is stored ×10.
@Component
public class TennisProfile extends SportProfile {

    private static final List<StatDefinition> STATS = List.of(
            new StatDefinition(1, "Wins",                      "Wins",    1,  true,  false, ""),
            new StatDefinition(2, "Ranking (lower is better)", "Ranking", 1,  false, true,  "", "Unranked"),
            new StatDefinition(3, "Aces",                      "Aces",    1,  true,  false, ""),
            new StatDefinition(4, "1st Serve % (×10)",         "1st Serve", 10, true, false, "%")
    );

    @Override
    public String getSport() { return "Tennis"; }

    @Override
    public List<StatDefinition> getStatDefinitions() { return STATS; }

    @Override
    public String getDescription() {
        return "Wins, ranking, aces and first-serve percentage. A lower ranking number is better "
             + "(a ranking of 0 is treated as unranked); 1st Serve % is stored ×10.";
    }

    @Override  // Method Overriding
    public List<DerivedMetric> derivedMetrics(PlayerStatistics s) {
        return List.of(
                new DerivedMetric("Ranking band", rankingBand(s.getStat2()), "Grouped from the stored ranking"),
                new DerivedMetric("1st serve percentage", String.format(Locale.UK, "%.1f%%", s.getStat4() / 10.0),
                        "Stored value ÷ 10")
        );
    }

    static String rankingBand(int ranking) {
        if (ranking <= 0) return "Unranked";
        if (ranking <= 10) return "Top 10";
        if (ranking <= 50) return "Top 50";
        if (ranking <= 100) return "Top 100";
        return "Outside top 100";
    }
}
