package com.fsms.service;

import com.fsms.analytics.*;
import com.fsms.model.Player;
import com.fsms.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// ============================================================
// PerformanceAnalyticsService — sport-specific performance insights.
//
// Rules:
//   - Uses ONLY the statistics already stored in the players table.
//     No external, live or generated data.
//   - Players are only ever measured against players of the SAME sport.
//   - Each statistic follows its own direction (StatDefinition):
//     lower economy rate (Cricket) and lower ranking (Tennis) are better.
//   - ×10 statistics are decoded before they are displayed.
//
// Score (0..100) for a stat = min-max normalisation of the value among
// all players of that sport, flipped for "lower is better" stats.
// Performance Index = average of the player's available stat scores.
// It is a RELATIVE measure inside this database, not a real-world rating.
//
// MVC Pattern: Service layer. Polymorphism: works only through the
// abstract SportProfile type.
// ============================================================
@Service
public class PerformanceAnalyticsService {

    private final PlayerRepository playerRepository;
    private final SportProfileRegistry registry;

    public PerformanceAnalyticsService(PlayerRepository playerRepository, SportProfileRegistry registry) {
        this.playerRepository = playerRepository;
        this.registry = registry;
    }

    public List<String> getSports() {
        return registry.getSports();
    }

    public SportProfile getProfile(String sport) {
        return registry.get(sport);
    }

    // All players of one sport, read from the database
    public List<Player> getPlayersForSport(String sport) {
        SportProfile profile = registry.get(sport);
        if (profile == null) return new ArrayList<>();
        return playerRepository.findBySportIgnoreCase(profile.getSport());
    }

    // ============================================================
    // Database-backed entry points (used by controllers)
    // ============================================================

    public SportSummary summarizeSport(String sport) {
        SportProfile profile = registry.get(sport);
        if (profile == null) return null;
        return summarize(profile, getPlayersForSport(profile.getSport()));
    }

    public PlayerInsight analysePlayer(Player player) {
        if (player == null || registry.get(player.getSport()) == null) return null;
        return analyse(player, getPlayersForSport(player.getSport()));
    }

    // Returns null when the two players are not from the same sport
    public PlayerComparison comparePlayers(Player a, Player b) {
        if (!isSameSport(a, b)) return null;
        return compare(a, b, getPlayersForSport(a.getSport()));
    }

    public boolean isSameSport(Player a, Player b) {
        return a != null && b != null && a.getSport() != null
                && a.getSport().equalsIgnoreCase(b.getSport())
                && registry.supports(a.getSport());
    }

    // ============================================================
    // Pure calculations (no database access) — unit-tested directly
    // ============================================================

    public SportSummary summarize(SportProfile profile, List<Player> players) {
        List<StatSummary> summaries = new ArrayList<>();
        for (StatDefinition def : profile.getStatDefinitions()) {
            summaries.add(summarizeStat(def, players));
        }

        // Overall leaderboard by performance index
        List<RankedEntry> leaderboard = new ArrayList<>();
        List<Player> sorted = new ArrayList<>(players);
        List<Integer> indexes = new ArrayList<>();
        for (Player p : sorted) indexes.add(performanceIndex(profile, p, players));
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) order.add(i);
        order.sort(Comparator.<Integer>comparingInt(indexes::get).reversed()
                .thenComparing(i -> sorted.get(i).getName(), Comparator.nullsLast(String::compareToIgnoreCase)));
        int rank = 0;
        int previous = Integer.MIN_VALUE;
        for (int pos = 0; pos < order.size(); pos++) {
            int i = order.get(pos);
            int idx = indexes.get(i);
            if (idx != previous) rank = pos + 1;
            previous = idx;
            leaderboard.add(new RankedEntry(rank, sorted.get(i), String.valueOf(idx), idx));
        }

        String avgAge = "—";
        long valued = 0;
        if (!players.isEmpty()) {
            double totalAge = 0;
            for (Player p : players) {
                totalAge += p.getAge();
                if (p.getEstimatedValue() != null) valued++;
            }
            avgAge = String.format(Locale.UK, "%.1f", totalAge / players.size());
        }
        return new SportSummary(profile, players.size(), avgAge, valued, summaries, leaderboard);
    }

    public PlayerInsight analyse(Player player, List<Player> peers) {
        SportProfile profile = registry.get(player.getSport());
        if (profile == null) return null;
        List<Player> pool = withPlayer(peers, player);

        List<StatInsight> insights = new ArrayList<>();
        for (StatDefinition def : profile.getStatDefinitions()) {
            List<Integer> values = values(def, pool);
            int raw = def.rawValue(player.getStatistics());
            int score = score(def, raw, values);
            int rank = rank(def, raw, values);
            int rankedCount = countReal(def, values);
            Double avg = average(def, values);
            String avgText = avg == null ? "—" : def.formatDecoded(avg);
            String status;
            if (def.isMissing(raw) || avg == null) {
                status = "No data";
            } else {
                double decoded = def.displayValue(raw);
                double diff = def.isHigherIsBetter() ? decoded - avg : avg - decoded;
                double tolerance = Math.abs(avg) * 0.02;
                status = diff > tolerance ? "Above average" : (diff < -tolerance ? "Below average" : "Average");
            }
            insights.add(new StatInsight(def, raw, def.format(raw), avgText, score, rank, rankedCount, status));
        }

        StatInsight strongest = null;
        StatInsight weakest = null;
        for (StatInsight s : insights) {
            if (s.isMissing()) continue;
            if (strongest == null || s.getScore() > strongest.getScore()) strongest = s;
            if (weakest == null || s.getScore() < weakest.getScore()) weakest = s;
        }
        if (strongest != null && strongest == weakest) weakest = null;

        return new PlayerInsight(player, profile, insights,
                profile.derivedMetrics(player.getStatistics()),
                performanceIndex(profile, player, pool), pool.size(), strongest, weakest);
    }

    public PlayerComparison compare(Player a, Player b, List<Player> peers) {
        if (!isSameSport(a, b)) {
            throw new IllegalArgumentException("Players can only be compared within the same sport");
        }
        SportProfile profile = registry.get(a.getSport());
        List<Player> pool = withPlayer(withPlayer(peers, a), b);

        List<ComparisonRow> rows = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> scoresA = new ArrayList<>();
        List<Integer> scoresB = new ArrayList<>();
        int winsA = 0, winsB = 0, ties = 0;

        for (StatDefinition def : profile.getStatDefinitions()) {
            List<Integer> values = values(def, pool);
            int rawA = def.rawValue(a.getStatistics());
            int rawB = def.rawValue(b.getStatistics());
            int sA = score(def, rawA, values);
            int sB = score(def, rawB, values);
            int cmp = def.compareRaw(rawA, rawB);
            String winner = cmp > 0 ? ComparisonRow.A : (cmp < 0 ? ComparisonRow.B : ComparisonRow.TIE);
            if (cmp > 0) winsA++; else if (cmp < 0) winsB++; else ties++;
            rows.add(new ComparisonRow(def, def.format(rawA), def.format(rawB), sA, sB, winner));
            labels.add(def.getShortLabel());
            scoresA.add(sA);
            scoresB.add(sB);
        }

        return new PlayerComparison(a, b, profile, rows, winsA, winsB, ties,
                performanceIndex(profile, a, pool), performanceIndex(profile, b, pool),
                new RadarChart(labels, scoresA, scoresB));
    }

    // ============================================================
    // Helpers
    // ============================================================

    private StatSummary summarizeStat(StatDefinition def, List<Player> players) {
        List<Integer> values = values(def, players);
        Double avg = average(def, values);

        List<Player> real = new ArrayList<>();
        for (Player p : players) {
            if (!def.isMissing(def.rawValue(p.getStatistics()))) real.add(p);
        }
        // Best first, according to the stat's own direction
        real.sort((p1, p2) -> def.compareRaw(def.rawValue(p2.getStatistics()), def.rawValue(p1.getStatistics())));

        List<RankedEntry> leaders = new ArrayList<>();
        for (Player p : real) {
            if (leaders.size() == 5) break;
            int raw = def.rawValue(p.getStatistics());
            leaders.add(new RankedEntry(rank(def, raw, values), p, def.format(raw), score(def, raw, values)));
        }
        return new StatSummary(def, avg == null ? "—" : def.formatDecoded(avg),
                leaders.isEmpty() ? null : leaders.get(0), leaders, real.size());
    }

    int performanceIndex(SportProfile profile, Player player, List<Player> pool) {
        int total = 0;
        int count = 0;
        for (StatDefinition def : profile.getStatDefinitions()) {
            int raw = def.rawValue(player.getStatistics());
            if (def.isMissing(raw)) continue;
            total += score(def, raw, values(def, pool));
            count++;
        }
        return count == 0 ? 0 : Math.round((float) total / count);
    }

    // 0..100: 100 = best value of this stat in the sport, 0 = worst
    static int score(StatDefinition def, int raw, List<Integer> values) {
        if (def.isMissing(raw)) return 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int v : values) {
            if (def.isMissing(v)) continue;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        if (min == Integer.MAX_VALUE || max == min) return 100;
        double fraction = def.isHigherIsBetter()
                ? (double) (raw - min) / (max - min)
                : (double) (max - raw) / (max - min);
        return (int) Math.round(Math.max(0, Math.min(1, fraction)) * 100);
    }

    // 1 + number of players strictly better; 0 when the value is missing
    static int rank(StatDefinition def, int raw, List<Integer> values) {
        if (def.isMissing(raw)) return 0;
        int better = 0;
        for (int v : values) {
            if (!def.isMissing(v) && def.compareRaw(v, raw) > 0) better++;
        }
        return better + 1;
    }

    private static int countReal(StatDefinition def, List<Integer> values) {
        int n = 0;
        for (int v : values) if (!def.isMissing(v)) n++;
        return n;
    }

    // Average of the DECODED values (×10 stats divided by 10), ignoring missing values
    private static Double average(StatDefinition def, List<Integer> values) {
        double total = 0;
        int n = 0;
        for (int v : values) {
            if (def.isMissing(v)) continue;
            total += def.displayValue(v);
            n++;
        }
        return n == 0 ? null : total / n;
    }

    private static List<Integer> values(StatDefinition def, List<Player> players) {
        List<Integer> values = new ArrayList<>();
        for (Player p : players) values.add(def.rawValue(p.getStatistics()));
        return values;
    }

    // Makes sure the player itself is part of the comparison pool
    private static List<Player> withPlayer(List<Player> peers, Player player) {
        List<Player> pool = new ArrayList<>(peers);
        for (Player p : peers) {
            if (p == player || (p.getId() != null && p.getId().equals(player.getId()))) return pool;
        }
        pool.add(player);
        return pool;
    }
}
