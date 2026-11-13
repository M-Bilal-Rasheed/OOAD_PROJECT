package com.fsms.analytics;

import com.fsms.model.PlayerStatistics;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

// Inheritance: FootballProfile EXTENDS SportProfile.
@Component
public class FootballProfile extends SportProfile {

    private static final List<StatDefinition> STATS = List.of(
            new StatDefinition(1, "Goals",                "Goals",       1, true, false, ""),
            new StatDefinition(2, "Assists",              "Assists",     1, true, false, ""),
            new StatDefinition(3, "Appearances",          "Apps",        1, true, false, ""),
            new StatDefinition(4, "Speed Rating (1-100)", "Speed",       1, true, false, "")
    );

    @Override
    public String getSport() { return "Football"; }

    @Override
    public List<StatDefinition> getStatDefinitions() { return STATS; }

    @Override
    public String getDescription() {
        return "Goals, assists, appearances and speed rating. Every football statistic is better when higher.";
    }

    @Override  // Method Overriding
    public List<DerivedMetric> derivedMetrics(PlayerStatistics s) {
        int involvements = s.getStat1() + s.getStat2();
        String perApp = s.getStat3() == 0 ? "—"
                : String.format(Locale.UK, "%.2f", ratio(s.getStat1(), s.getStat3()));
        String involvementsPerApp = s.getStat3() == 0 ? "—"
                : String.format(Locale.UK, "%.2f", ratio(involvements, s.getStat3()));
        return List.of(
                new DerivedMetric("Goal involvements", String.valueOf(involvements), "Goals + assists"),
                new DerivedMetric("Goals per appearance", perApp, "Goals ÷ appearances"),
                new DerivedMetric("Involvements per appearance", involvementsPerApp, "(Goals + assists) ÷ appearances")
        );
    }
}
