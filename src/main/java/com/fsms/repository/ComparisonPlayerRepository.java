package com.fsms.repository;

import com.fsms.model.ComparisonPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// Repository for ComparisonPlayer — database access only.
@Repository
public interface ComparisonPlayerRepository extends JpaRepository<ComparisonPlayer, Long> {

    // Used when an Admin deletes a player: comparison_players.player_id has
    // ON DELETE RESTRICT, so those rows must go before the player does.
    @Modifying
    @Query("DELETE FROM ComparisonPlayer cp WHERE cp.playerId = :playerId")
    int deleteByPlayerId(@Param("playerId") Long playerId);
}
