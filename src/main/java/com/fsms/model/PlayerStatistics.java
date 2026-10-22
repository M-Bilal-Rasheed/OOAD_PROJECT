package com.fsms.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;

// ============================================================
// Composition: PlayerStatistics is OWNED BY Player.
// It is marked @Embeddable so JPA stores its columns inside the
// same 'players' database table. It cannot exist without a Player.
//
// The four generic fields (stat1..stat4) hold sport-specific values:
//   Football:   goals, assists, appearances, speed rating
//   Cricket:    runs, wickets, batting average (×10), economy rate (×10)
//   Basketball: points, assists, rebounds, shooting % (×10)
//   Tennis:     wins, ranking (inverted), aces, first-serve % (×10)
// ============================================================
@Embeddable  // Composition — embedded inside Player
public class PlayerStatistics {

    // Encapsulation: private fields
    // Each field stores a sport-specific statistic (see Player.getSportStatLabels())
    @Min(value = 0, message = "Stat 1 must be 0 or greater")
    private int stat1;

    @Min(value = 0, message = "Stat 2 must be 0 or greater")
    private int stat2;

    @Min(value = 0, message = "Stat 3 must be 0 or greater")
    private int stat3;

    @Min(value = 0, message = "Stat 4 must be 0 or greater")
    private int stat4;

    // Default constructor required by JPA
    public PlayerStatistics() {
    }

    // Constructor for easy creation
    public PlayerStatistics(int stat1, int stat2, int stat3, int stat4) {
        this.stat1 = stat1;
        this.stat2 = stat2;
        this.stat3 = stat3;
        this.stat4 = stat4;
    }

    // Getters and Setters — Encapsulation
    public int getStat1() { return stat1; }
    public void setStat1(int stat1) { this.stat1 = stat1; }

    public int getStat2() { return stat2; }
    public void setStat2(int stat2) { this.stat2 = stat2; }

    public int getStat3() { return stat3; }
    public void setStat3(int stat3) { this.stat3 = stat3; }

    public int getStat4() { return stat4; }
    public void setStat4(int stat4) { this.stat4 = stat4; }
}
