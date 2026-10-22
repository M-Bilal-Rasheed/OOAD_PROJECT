package com.fsms.model;

// ============================================================
// Inheritance: User is the PARENT (base) class.
// Scout and Admin both extend this class.
// ============================================================
public class User {

    // Encapsulation: private fields, accessed via getters/setters
    private String username;
    private String role;

    // Constructor
    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    // Getters and Setters — Encapsulation
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Simple display method — can be overridden by child classes
    public String getDisplayInfo() {
        return "User: " + username + " | Role: " + role;
    }
}
