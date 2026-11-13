package com.fsms.analytics;

import com.fsms.model.PlayerStatistics;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

// Inheritance: BasketballProfile EXTENDS SportProfile.
// Shooting % is stored ×10 (e.g. 47.5% is stored as 475).
@Component
public class BasketballProfile extends SportProfile {

    private static final List<StatDefinition> STATS = List.of(
            new StatDefinition(1, "Points",           "Points",   1,  true, false, ""),
            new StatDefinition(2, "Assists",          "Assists",  1,  true, false, ""),
            new StatDefinition(3, "Rebounds",         "Rebounds", 1,  true, false, ""),
            new StatDefinition(4, "Shooting % (×10)", "Shooting", 10, true, false, "%")
    );

    @Override
    public String getSport() { return "Basketball"; }

    @Override
    public List<StatDefinition> getStatDefinitions() { return STATS; }

    @Override
    public String getDescription() {
        return "Points, assists, rebounds and shooting percentage. Shooting % is stored ×10 and shown here as a real percentage.";
    }

    @Override  // Method Overriding
    public List<DerivedMetric> derivedMetrics(PlayerStatistics s) {
        int combined = s.getStat1() + s.getStat2() + s.getStat3();
        return List.of(
                new DerivedMetric("Combined contributions", String.format(Locale.UK, "%,d", combined),
                        "Points + assists + rebounds"),
                new DerivedMetric("Shooting percentage", String.format(Locale.UK, "%.1f%%", s.getStat4() / 10.0),
                        "Stored value ÷ 10")
        );
    }
}
