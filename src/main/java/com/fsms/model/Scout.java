package com.fsms.model;

import java.util.ArrayList;
import java.util.List;

// ============================================================
// Inheritance: Scout EXTENDS User (child class of User).
// Scout can search players, manage a shortlist, and run valuations.
// ============================================================
public class Scout extends User {  // Inheritance

    // ============================================================
    // Aggregation: Scout holds a LIST of Player objects.
    // These Player objects exist in the database independently —
    // if the Scout is removed, the Players still exist.
    // This is Aggregation (has-a relationship, but not ownership).
    // ============================================================
    private List<Player> shortlist;  // Aggregation

    public Scout(String username) {
        super(username, "SCOUT");  // Calls the parent User constructor
        this.shortlist = new ArrayList<>();  // Initialise empty shortlist
    }

    // ============================================================
    // Method Overriding: @Override replaces getRole() from User.
    // ============================================================
    @Override  // Method Overriding
    public String getRole() {
        return "SCOUT";
    }

    // Override getDisplayInfo() to show Scout-specific message
    @Override  // Method Overriding
    public String getDisplayInfo() {
        return "Scout: " + getUsername() + " | Shortlist size: " + shortlist.size();
    }

    // Aggregation: Add a player reference to the shortlist
    public void addToShortlist(Player player) {  // Aggregation
        shortlist.add(player);
    }

    // Aggregation: Remove a player reference from the shortlist
    public void removeFromShortlist(Player player) {  // Aggregation
        shortlist.remove(player);
    }

    // Getter for the shortlist
    public List<Player> getShortlist() {
        return shortlist;
    }
}
