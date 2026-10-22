package com.fsms.repository;

import com.fsms.model.ShortlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// MVC Pattern: Repository layer handles all database access for ShortlistEntry.
@Repository
public interface ShortlistRepository extends JpaRepository<ShortlistEntry, Long> {

    // Find all shortlist entries belonging to a specific scout
    List<ShortlistEntry> findByScoutName(String scoutName);

    // Check if a specific scout already has a specific player shortlisted
    Optional<ShortlistEntry> findByScoutNameAndPlayerId(String scoutName, Long playerId);
}
