package com.fsms.repository;

import com.fsms.model.ScoutingReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository for ScoutingReport — database access only.
@Repository
public interface ScoutingReportRepository extends JpaRepository<ScoutingReport, Long> {

    // All reports written by one scout, most recently updated first
    List<ScoutingReport> findByAuthor_IdOrderByUpdatedAtDesc(Long authorId);

    // One scout's reports about one player
    List<ScoutingReport> findByAuthor_IdAndPlayer_IdOrderByUpdatedAtDesc(Long authorId, Long playerId);

    long countByAuthor_Id(Long authorId);

    // Used when an Admin deletes a player: the player's reports go with it
    @Modifying
    @Query("DELETE FROM ScoutingReport r WHERE r.player.id = :playerId")
    int deleteByPlayerId(@Param("playerId") Long playerId);
}
