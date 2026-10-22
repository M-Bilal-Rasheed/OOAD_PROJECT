package com.fsms.service;

import com.fsms.model.Player;
import com.fsms.model.ShortlistEntry;
import com.fsms.repository.PlayerRepository;
import com.fsms.repository.ShortlistRepository;
import com.fsms.valuation.ValuationStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ============================================================
// MVC Pattern: Service layer — contains all business logic.
// Controllers call service methods; service calls repositories.
// ============================================================
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final ShortlistRepository shortlistRepository;

    // ============================================================
    // Strategy Design Pattern: ValuationStrategy is injected here
    // as an interface reference. The actual implementation
    // (BasicValuationStrategy) is provided by Spring.
    // Swapping to a different strategy requires no change here.
    // ============================================================
    private final ValuationStrategy valuationStrategy;  // Strategy Design Pattern

    public PlayerService(PlayerRepository playerRepository,
                         ShortlistRepository shortlistRepository,
                         ValuationStrategy valuationStrategy) {
        this.playerRepository = playerRepository;
        this.shortlistRepository = shortlistRepository;
        this.valuationStrategy = valuationStrategy;
    }

    // ============================================================
    // PLAYER MANAGEMENT (Admin features)
    // ============================================================

    public Player addPlayer(Player player) {
        return playerRepository.save(player);
    }

    public Player updatePlayer(Player player) {
        return playerRepository.save(player);
    }

    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player getPlayerById(Long id) {
        Optional<Player> result = playerRepository.findById(id);
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    // ============================================================
    // DASHBOARD SUMMARY STATS
    // Returns total player count, count by sport, and count of
    // valued players — used by the Admin and Scout dashboards.
    // ============================================================

    public long getTotalPlayerCount() {
        return playerRepository.count();
    }

    public long getValuedPlayerCount() {
        int count = 0;
        for (Player p : playerRepository.findAll()) {
            if (p.getEstimatedValue() != null) {
                count++;
            }
        }
        return count;
    }

    // Count players for a specific sport
    public long getCountBySport(String sport) {
        int count = 0;
        for (Player p : playerRepository.findAll()) {
            if (sport.equals(p.getSport())) {
                count++;
            }
        }
        return count;
    }

    // ============================================================
    // SEARCH AND FILTER
    // ============================================================

    // Search players by name, sport, position, and/or team
    public List<Player> searchPlayers(String name, String sport, String position, String club) {
        if (name.isEmpty() && sport.isEmpty() && position.isEmpty() && club.isEmpty()) {
            return playerRepository.findAll();
        }
        return playerRepository.searchPlayers(name, sport, position, club);
    }

    // ============================================================
    // PLAYER VALUATION (Strategy Pattern in action)
    // ============================================================

    // Calculate and save a player's estimated value
    public Player calculateAndSaveValue(Long playerId) {
        Player player = getPlayerById(playerId);
        if (player == null) {
            return null;
        }

        // ============================================================
        // Polymorphism: calculateValue() is called on the interface
        // reference. The actual method executed is in BasicValuationStrategy.
        // ============================================================
        long value = valuationStrategy.calculateValue(player);  // Polymorphism + Strategy Pattern

        player.setEstimatedValue(value);
        return playerRepository.save(player);
    }

    // ============================================================
    // PLAYER COMPARISON
    // ============================================================

    public List<Player> comparePlayers(Long id1, Long id2) {
        List<Player> twoPlayers = new ArrayList<>();
        Player player1 = getPlayerById(id1);
        Player player2 = getPlayerById(id2);
        if (player1 != null) twoPlayers.add(player1);
        if (player2 != null) twoPlayers.add(player2);
        return twoPlayers;
    }

    // ============================================================
    // SCOUT SHORTLIST MANAGEMENT (Aggregation in action)
    // ============================================================

    // Add a player to a scout's shortlist — returns false if already there
    public boolean addToShortlist(String scoutName, Long playerId) {
        Optional<ShortlistEntry> existing = shortlistRepository.findByScoutNameAndPlayerId(scoutName, playerId);
        if (existing.isPresent()) {
            return false;
        }
        ShortlistEntry entry = new ShortlistEntry(scoutName, playerId);
        shortlistRepository.save(entry);
        return true;
    }

    public void removeFromShortlist(Long entryId) {
        shortlistRepository.deleteById(entryId);
    }

    // Returns Player objects on the scout's shortlist
    // Aggregation — players exist independently, this just collects references
    public List<Player> getShortlist(String scoutName) {
        List<ShortlistEntry> entries = shortlistRepository.findByScoutName(scoutName);
        List<Player> shortlistedPlayers = new ArrayList<>();
        for (ShortlistEntry entry : entries) {
            Player player = getPlayerById(entry.getPlayerId());
            if (player != null) {
                shortlistedPlayers.add(player);  // Aggregation
            }
        }
        return shortlistedPlayers;
    }

    public Long getShortlistEntryId(String scoutName, Long playerId) {
        Optional<ShortlistEntry> entry = shortlistRepository.findByScoutNameAndPlayerId(scoutName, playerId);
        if (entry.isPresent()) {
            return entry.get().getId();
        }
        return null;
    }

    public boolean isOnShortlist(String scoutName, Long playerId) {
        return shortlistRepository.findByScoutNameAndPlayerId(scoutName, playerId).isPresent();
    }
}
