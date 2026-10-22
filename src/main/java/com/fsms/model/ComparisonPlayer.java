package com.fsms.model;

import jakarta.persistence.*;

// ============================================================
// ComparisonPlayer is a join-table entity linking a Comparison
// to a Player. Each row means "this player was in that comparison".
// ============================================================
@Entity
@Table(name = "comparison_players",
       uniqueConstraints = @UniqueConstraint(columnNames = {"comparison_id", "player_id"}))
public class ComparisonPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many ComparisonPlayer rows belong to one Comparison
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comparison_id", nullable = false)
    private Comparison comparison;

    // The player referenced in this comparison entry
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    public ComparisonPlayer() {
    }

    public ComparisonPlayer(Comparison comparison, Long playerId) {
        this.comparison = comparison;
        this.playerId = playerId;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Comparison getComparison() { return comparison; }
    public void setComparison(Comparison comparison) { this.comparison = comparison; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
}
