package com.fsms.model;

import jakarta.persistence.*;

// ============================================================
// ShortlistEntry stores which scout has which player on their list.
// This is the database-backed version of the Scout's shortlist (Aggregation).
// The Player record is NOT deleted when a ShortlistEntry is deleted.
// ============================================================
@Entity
@Table(name = "shortlist")
public class ShortlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The name of the scout who owns this entry
    private String scoutName;

    // The ID of the player on the shortlist (Aggregation — just a reference)
    private Long playerId;

    // Default constructor required by JPA
    public ShortlistEntry() {
    }

    public ShortlistEntry(String scoutName, Long playerId) {
        this.scoutName = scoutName;
        this.playerId = playerId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScoutName() {
        return scoutName;
    }

    public void setScoutName(String scoutName) {
        this.scoutName = scoutName;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
}
