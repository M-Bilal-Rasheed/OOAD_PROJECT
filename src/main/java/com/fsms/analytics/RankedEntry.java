package com.fsms.analytics;

import com.fsms.model.Player;

// One row of a leaderboard: a player, their value and their 0..100 score.
public final class RankedEntry {

    private final int rank;
    private final Player player;
    private final String formattedValue;
    private final int score;

    public RankedEntry(int rank, Player player, String formattedValue, int score) {
        this.rank = rank;
        this.player = player;
        this.formattedValue = formattedValue;
        this.score = score;
    }

    public int getRank() { return rank; }
    public Player getPlayer() { return player; }
    public String getFormattedValue() { return formattedValue; }
    public int getScore() { return score; }
    // Bars never collapse fully so the label stays readable
    public int getBarWidth() { return Math.max(4, score); }
}
