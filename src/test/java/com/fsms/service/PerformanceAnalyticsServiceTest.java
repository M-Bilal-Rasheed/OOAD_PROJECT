package com.fsms.service;

import com.fsms.analytics.*;
import com.fsms.model.Player;
import com.fsms.model.PlayerStatistics;
import com.fsms.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

// Pure unit tests for the analytics rules — no database, no Spring context.
class PerformanceAnalyticsServiceTest {

    private PerformanceAnalyticsService service;
    private SportProfileRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SportProfileRegistry(List.of(
                new TennisProfile(), new FootballProfile(), new BasketballProfile(), new CricketProfile()));
        service = new PerformanceAnalyticsService(mock(PlayerRepository.class), registry);
    }

    private static long nextId = 1;

    private static Player player(String name, String sport, int s1, int s2, int s3, int s4) {
        Player p = new Player();
        p.setId(nextId++);
        p.setName(name);
        p.setSport(sport);
        p.setAge(25);
        p.setPosition("Pos");
        p.setClub("Club");
        p.setNationality("Nat");
        p.setStatistics(new PlayerStatistics(s1, s2, s3, s4));
        return p;
    }

    @Test
    void cricketLowerEconomyRateWins() {
        // Same runs/wickets/average; only economy differs: 6.5 vs 8.2
        Player a = player("Economical", "Cricket", 500, 20, 350, 65);
        Player b = player("Expensive",  "Cricket", 500, 20, 350, 82);
        PlayerComparison c = service.compare(a, b, List.of(a, b));

        ComparisonRow economy = c.getRows().get(3);
        assertEquals("Economy Rate (×10)", economy.getDefinition().getLabel());
        assertEquals(ComparisonRow.A, economy.getWinner(), "lower economy rate must win");
        assertEquals(100, economy.getScoreA());
        assertEquals(0, economy.getScoreB());
        assertEquals(1, c.getWinsA());
        assertEquals(0, c.getWinsB());
        assertEquals(3, c.getTies());
    }

    @Test
    void tennisLowerRankingWinsAndZeroIsUnranked() {
        Player top = player("Top", "Tennis", 40, 3, 200, 650);
        Player low = player("Low", "Tennis", 40, 80, 200, 650);
        Player unranked = player("None", "Tennis", 40, 0, 200, 650);

        PlayerComparison c = service.compare(top, low, List.of(top, low, unranked));
        assertEquals(ComparisonRow.A, c.getRows().get(1).getWinner(), "ranking 3 beats ranking 80");

        PlayerComparison c2 = service.compare(unranked, low, List.of(top, low, unranked));
        assertEquals(ComparisonRow.B, c2.getRows().get(1).getWinner(), "an unranked player never wins ranking");
        assertEquals("Unranked", c2.getRows().get(1).getFormattedA());
    }

    @Test
    void cricketZeroEconomyMeansDidNotBowlAndNeverWins() {
        Player batter = player("Batter", "Cricket", 1200, 0, 450, 0);
        Player bowler = player("Bowler", "Cricket", 80, 100, 100, 75);
        PlayerComparison c = service.compare(batter, bowler, List.of(batter, bowler));
        assertEquals("Did not bowl", c.getRows().get(3).getFormattedA());
        assertEquals(ComparisonRow.B, c.getRows().get(3).getWinner());

        SportSummary s = service.summarize(registry.get("Cricket"), List.of(batter, bowler));
        assertEquals("7.5", s.getStatSummaries().get(3).getAverageFormatted(), "0 economy excluded from average");
        assertEquals("Bowler", s.getStatSummaries().get(3).getBest().getPlayer().getName());
        assertEquals("No data", service.analyse(batter, List.of(batter, bowler)).getStats().get(3).getStatus());
    }

    @Test
    void scaledStatisticsAreDecodedForDisplay() {
        Player bat = player("Bat", "Cricket", 1200, 2, 452, 74);
        PlayerInsight insight = service.analyse(bat, List.of(bat));
        assertEquals("45.2", insight.getStats().get(2).getFormattedValue());
        assertEquals("7.4", insight.getStats().get(3).getFormattedValue());

        Player hoop = player("Hoop", "Basketball", 900, 300, 400, 475);
        assertEquals("47.5%", service.analyse(hoop, List.of(hoop)).getStats().get(3).getFormattedValue());
    }

    @Test
    void sportAverageUsesDecodedValues() {
        Player a = player("A", "Cricket", 100, 1, 300, 60);
        Player b = player("B", "Cricket", 300, 3, 500, 80);
        SportSummary summary = service.summarize(registry.get("Cricket"), List.of(a, b));
        assertEquals("200", summary.getStatSummaries().get(0).getAverageFormatted());
        assertEquals("40.0", summary.getStatSummaries().get(2).getAverageFormatted());
        assertEquals("7.0", summary.getStatSummaries().get(3).getAverageFormatted());
        // Economy leaderboard: lowest first
        assertEquals("A", summary.getStatSummaries().get(3).getLeaders().get(0).getPlayer().getName());
    }

    @Test
    void comparisonAcrossSportsIsRejected() {
        Player f = player("F", "Football", 10, 5, 30, 80);
        Player t = player("T", "Tennis", 10, 5, 30, 600);
        assertFalse(service.isSameSport(f, t));
        assertNull(service.comparePlayers(f, t));
        assertThrows(IllegalArgumentException.class, () -> service.compare(f, t, List.of(f, t)));
    }

    @Test
    void performanceIndexAndStrongestWeakest() {
        Player best = player("Best", "Football", 30, 15, 40, 90);
        Player worst = player("Worst", "Football", 2, 1, 10, 50);
        Player mixed = player("Mixed", "Football", 30, 1, 10, 50);
        List<Player> pool = List.of(best, worst, mixed);

        assertEquals(100, service.analyse(best, pool).getPerformanceIndex());
        assertEquals(0, service.analyse(worst, pool).getPerformanceIndex());
        PlayerInsight m = service.analyse(mixed, pool);
        assertEquals("Goals", m.getStrongest().getDefinition().getLabel());
        assertEquals(1, m.getStats().get(0).getRank(), "tied best goals share rank 1");
    }

    @Test
    void singlePlayerSportScoresFull() {
        Player only = player("Only", "Basketball", 10, 10, 10, 400);
        PlayerInsight i = service.analyse(only, List.of());
        assertEquals(1, i.getPeerCount());
        assertEquals(100, i.getPerformanceIndex());
    }

    @Test
    void profileLabelsMatchPlayerLabelsForEverySport() {
        // Parity: the labels shown on profiles, forms and analytics must agree
        for (String sport : registry.getSports()) {
            Player p = player("X", sport, 0, 0, 0, 0);
            assertArrayEquals(p.getSportStatLabels(), registry.get(sport).getLabels(), sport);
        }
    }

    @Test
    void footballDerivedMetricsUseStoredStatsOnly() {
        Player p = player("Nine", "Football", 12, 8, 40, 70);
        List<DerivedMetric> m = registry.get("Football").derivedMetrics(p.getStatistics());
        assertEquals("20", m.get(0).getValue());
        assertEquals("0.30", m.get(1).getValue());
        assertEquals("0.50", m.get(2).getValue());

        Player noApps = player("New", "Football", 0, 0, 0, 60);
        assertEquals("—", registry.get("Football").derivedMetrics(noApps.getStatistics()).get(1).getValue());
    }
}
