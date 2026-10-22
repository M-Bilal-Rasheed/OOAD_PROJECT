package com.fsms.service;

import com.fsms.model.Comparison;
import com.fsms.model.ComparisonPlayer;
import com.fsms.model.Player;
import com.fsms.repository.ComparisonRepository;
import com.fsms.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ============================================================
// ComparisonService handles saving and loading comparisons.
// MVC Pattern: Service layer — business logic only.
// ============================================================
@Service
public class ComparisonService {

    private final ComparisonRepository comparisonRepository;
    private final PlayerRepository playerRepository;

    public ComparisonService(ComparisonRepository comparisonRepository,
                             PlayerRepository playerRepository) {
        this.comparisonRepository = comparisonRepository;
        this.playerRepository = playerRepository;
    }

    // Save a new comparison for the given user with a list of player IDs
    public Comparison saveComparison(Long userId, String title, Long player1Id, Long player2Id) {
        Comparison comparison = new Comparison();
        comparison.setUserId(userId);
        comparison.setTitle(title != null && !title.isBlank() ? title.trim() : "Comparison");

        // Add both players as ComparisonPlayer entries (Composition)
        ComparisonPlayer cp1 = new ComparisonPlayer(comparison, player1Id);
        ComparisonPlayer cp2 = new ComparisonPlayer(comparison, player2Id);
        comparison.getComparisonPlayers().add(cp1);
        comparison.getComparisonPlayers().add(cp2);

        return comparisonRepository.save(comparison);
    }

    // Get all comparisons saved by a specific user
    public List<Comparison> getComparisonsForUser(Long userId) {
        return comparisonRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Load a specific comparison — only if it belongs to the given user
    public Comparison getComparisonForUser(Long comparisonId, Long userId) {
        Optional<Comparison> result = comparisonRepository.findById(comparisonId);
        if (result.isPresent() && result.get().getUserId().equals(userId)) {
            return result.get();
        }
        return null;  // Not found or not owned by this user
    }

    // Delete a saved comparison — only if it belongs to the given user
    public boolean deleteComparison(Long comparisonId, Long userId) {
        Comparison comparison = getComparisonForUser(comparisonId, userId);
        if (comparison != null) {
            comparisonRepository.delete(comparison);
            return true;
        }
        return false;
    }

    // Resolve the Player objects for a Comparison (for rendering the result page)
    public List<Player> getPlayersForComparison(Comparison comparison) {
        List<Player> players = new ArrayList<>();
        for (ComparisonPlayer cp : comparison.getComparisonPlayers()) {
            Optional<Player> player = playerRepository.findById(cp.getPlayerId());
            player.ifPresent(players::add);
        }
        return players;
    }
}
