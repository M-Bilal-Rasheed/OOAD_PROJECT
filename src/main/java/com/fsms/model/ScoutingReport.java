package com.fsms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ============================================================
// ScoutingReport — a scout's personal written assessment of a player.
//
// Association:
//   - Many ScoutingReports belong to ONE Player   (player_id FK)
//   - Many ScoutingReports are written by ONE AppUser (scout_id FK)
// Only the author may view, edit or delete a report; this rule is
// enforced in ScoutingReportService.
// Encapsulation: private fields with getters/setters.
// ============================================================
@Entity
@Table(name = "scouting_reports",
       indexes = {
           @Index(name = "idx_scouting_reports_scout", columnList = "scout_id"),
           @Index(name = "idx_scouting_reports_player", columnList = "player_id")
       })
public class ScoutingReport {

    public static final int TEXT_LIMIT = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The player this report is about
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    // The scout (user account) who wrote the report — the owner.
    // Stored in the scout_id column (the column name used by the existing table).
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "scout_id", nullable = false)
    private AppUser author;

    @Column(name = "strengths", nullable = false, length = TEXT_LIMIT)
    private String strengths;

    @Column(name = "weaknesses", nullable = false, length = TEXT_LIMIT)
    private String weaknesses;

    @Column(name = "potential", nullable = false, length = TEXT_LIMIT)
    private String potential;

    // Potential rating from 1 (low) to 10 (elite)
    @Column(name = "potential_rating", nullable = false)
    private int potentialRating;

    @Column(name = "performance_observations", nullable = false, length = TEXT_LIMIT)
    private String performanceObservations;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false, length = 32)
    private Recommendation recommendation;

    // Short justification of the overall recommendation
    @Column(name = "recommendation_summary", nullable = false, length = TEXT_LIMIT)
    private String recommendationSummary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ScoutingReport() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // True when the given user id is the author of this report
    public boolean isOwnedBy(Long userId) {
        return userId != null && author != null && userId.equals(author.getId());
    }

    public boolean isEdited() {
        return createdAt != null && updatedAt != null && updatedAt.isAfter(createdAt.plusSeconds(1));
    }

    // Getters and Setters — Encapsulation
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public AppUser getAuthor() { return author; }
    public void setAuthor(AppUser author) { this.author = author; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }

    public String getPotential() { return potential; }
    public void setPotential(String potential) { this.potential = potential; }

    public int getPotentialRating() { return potentialRating; }
    public void setPotentialRating(int potentialRating) { this.potentialRating = potentialRating; }

    public String getPerformanceObservations() { return performanceObservations; }
    public void setPerformanceObservations(String performanceObservations) {
        this.performanceObservations = performanceObservations;
    }

    public Recommendation getRecommendation() { return recommendation; }
    public void setRecommendation(Recommendation recommendation) { this.recommendation = recommendation; }

    public String getRecommendationSummary() { return recommendationSummary; }
    public void setRecommendationSummary(String recommendationSummary) {
        this.recommendationSummary = recommendationSummary;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
