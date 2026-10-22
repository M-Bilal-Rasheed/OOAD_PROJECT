package com.fsms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ============================================================
// Comparison stores a saved side-by-side player comparison.
// A user can save a comparison with a title for later review.
// Composition: Comparison owns its ComparisonPlayer entries.
// ============================================================
@Entity
@Table(name = "comparisons")
public class Comparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who created this comparison (links to AppUser.id)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Optional display title chosen by the user
    @Column(name = "title")
    private String title;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Composition: each Comparison owns its ComparisonPlayer entries.
    // CascadeType.ALL + orphanRemoval means entries are deleted with the comparison.
    @OneToMany(mappedBy = "comparison", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComparisonPlayer> comparisonPlayers = new ArrayList<>();

    public Comparison() {
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ComparisonPlayer> getComparisonPlayers() { return comparisonPlayers; }
    public void setComparisonPlayers(List<ComparisonPlayer> comparisonPlayers) {
        this.comparisonPlayers = comparisonPlayers;
    }
}
