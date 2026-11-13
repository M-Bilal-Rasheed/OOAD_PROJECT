package com.fsms.analytics;

import com.fsms.model.PlayerStatistics;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

// Inheritance: CricketProfile EXTENDS SportProfile.
// Batting average and economy rate are stored ×10.
// Economy rate is the only cricket stat where LOWER is better.
// A stored economy of 0 means the player has not bowled (a real economy
// rate is never 0), so it is shown as "Did not bowl" and is not ranked.
@Component
public class CricketProfile extends SportProfile {

    private static final List<StatDefinition> STATS = List.of(
            new StatDefinition(1, "Runs",                  "Runs",     1,  true,  false, ""),
            new StatDefinition(2, "Wickets",               "Wickets",  1,  true,  false, ""),
            new StatDefinition(3, "Batting Average (×10)", "Bat Avg",  10, true,  false, ""),
            new StatDefinition(4, "Economy Rate (×10)",    "Economy",  10, false, true,  "", "Did not bowl")
    );

    @Override
    public String getSport() { return "Cricket"; }

    @Override
    public List<StatDefinition> getStatDefinitions() { return STATS; }

    @Override
    public String getDescription() {
        return "Runs, wickets, batting average and economy rate. A lower economy rate is better and an economy "
             + "of 0 is treated as \"did not bowl\"; batting average and economy are stored ×10 and shown as real values.";
    }

    @Override  // Method Overriding
    public List<DerivedMetric> derivedMetrics(PlayerStatistics s) {
        return List.of(
                new DerivedMetric("Batting average",
                        String.format(Locale.UK, "%.1f", s.getStat3() / 10.0), "Stored value ÷ 10"),
                new DerivedMetric("Economy rate",
                        s.getStat4() == 0 ? "Did not bowl" : String.format(Locale.UK, "%.1f", s.getStat4() / 10.0),
                        "Runs conceded per over (lower is better)"),
                new DerivedMetric("Runs + wickets",
                        String.format(Locale.UK, "%,d / %d", s.getStat1(), s.getStat2()), "Batting and bowling output")
        );
    }
}
