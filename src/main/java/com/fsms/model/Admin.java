package com.fsms.model;

// ============================================================
// Inheritance: Admin EXTENDS User (child class of User).
// Admin has access to player management (add, edit, delete).
// ============================================================
public class Admin extends User {  // Inheritance

    public Admin(String username) {
        super(username, "ADMIN");  // Calls the parent User constructor
    }

    // ============================================================
    // Method Overriding: @Override means this replaces the
    // getRole() method defined in the parent User class.
    // ============================================================
    @Override  // Method Overriding
    public String getRole() {
        return "ADMIN";
    }

    // Override getDisplayInfo() to show Admin-specific message
    @Override  // Method Overriding
    public String getDisplayInfo() {
        return "Admin: " + getUsername() + " | Can manage all players";
    }
}
