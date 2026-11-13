package com.fsms.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// ============================================================
// Class and Object: Player is the main entity class.
// Player objects are created whenever a new player is added.
// Encapsulation: all fields are private with getters/setters.
// Composition: Player owns a PlayerStatistics object.
// ============================================================
@Entity  // JPA: maps this class to the 'players' database table
@Table(name = "players")
public class Player {

    // Encapsulation: private fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Min(value = 1, message = "Age must be at least 1")
    private int age;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    // Sport: Football, Cricket, Basketball, Tennis
    @NotBlank(message = "Sport is required")
    private String sport;

    // Position label depends on sport (e.g. Striker, Batsman, Point Guard)
    @NotBlank(message = "Position is required")
    private String position;

    // Team or club name
    @NotBlank(message = "Team is required")
    private String club;

    // Estimated value — calculated by ValuationStrategy
    private Long estimatedValue;

    // ============================================================
    // Composition: Player OWNS a PlayerStatistics object.
    // @Embedded stores the stats columns in the same 'players' table.
    // If the Player is deleted, the stats are also gone — Composition.
    // ============================================================
    @Embedded   // Composition
    @Valid
    @NotNull(message = "Player statistics are required")
    private PlayerStatistics statistics;

    // Default constructor required by JPA
    public Player() {
        this.statistics = new PlayerStatistics();  // Composition: always create together
    }

    // ============================================================
    // Returns the four stat labels appropriate for this player's sport.
    // Used by Thymeleaf templates to show the correct stat names.
    // ============================================================
    public String[] getSportStatLabels() {
        if (sport == null) {
            return new String[]{"Stat 1", "Stat 2", "Stat 3", "Stat 4"};
        }
        switch (sport) {
            case "Football":
                return new String[]{"Goals", "Assists", "Appearances", "Speed Rating (1-100)"};
            case "Cricket":
                return new String[]{"Runs", "Wickets", "Batting Average (×10)", "Economy Rate (×10)"};
            case "Basketball":
                return new String[]{"Points", "Assists", "Rebounds", "Shooting % (×10)"};
            case "Tennis":
                return new String[]{"Wins", "Ranking (lower is better)", "Aces", "1st Serve % (×10)"};
            default:
                return new String[]{"Stat 1", "Stat 2", "Stat 3", "Stat 4"};
        }
    }

    // Getters and Setters — Encapsulation

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getClub() { return club; }
    public void setClub(String club) { this.club = club; }

    public Long getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(Long estimatedValue) { this.estimatedValue = estimatedValue; }

    // Composition: access the owned PlayerStatistics
    public PlayerStatistics getStatistics() { return statistics; }  // Composition
    public void setStatistics(PlayerStatistics statistics) { this.statistics = statistics; }
}
