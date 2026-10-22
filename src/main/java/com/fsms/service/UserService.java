package com.fsms.service;

import com.fsms.model.AppUser;
import com.fsms.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

// ============================================================
// UserService handles all user-account operations:
//   - Signup (with BCrypt hashing, duplicate-email check)
//   - Login verification (password check)
//   - Admin seeding (safe one-time setup from env vars)
// MVC Pattern: Service layer — business logic only.
// ============================================================
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // BCrypt injected from SecurityConfig

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ============================================================
    // SIGNUP
    // Public signup always creates a SCOUT account.
    // Returns null on success, or an error message string on failure.
    // ============================================================
    public String registerScout(String fullName, String email, String rawPassword) {

        // Server-side validation
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Full name is required.";
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            return "A valid email address is required.";
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            return "Password must be at least 6 characters.";
        }

        // Duplicate email check
        if (userRepository.findByEmail(email.trim().toLowerCase()).isPresent()) {
            return "An account with this email address already exists.";
        }

        // Create the account — role is always SCOUT from public signup
        AppUser user = new AppUser();
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));  // BCrypt hash
        user.setRole("SCOUT");  // Public signup always creates SCOUT — never ADMIN

        userRepository.save(user);
        logger.info("[SIGNUP] Scout account created: {}", user.getEmail());
        return null;  // null = success
    }

    // ============================================================
    // LOGIN VERIFICATION
    // Finds the user by email and checks the BCrypt password.
    // Returns the AppUser if credentials are valid, null otherwise.
    // ============================================================
    public AppUser verifyLogin(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            logger.warn("[LOGIN] Email or password is null");
            return null;
        }
        String normalizedEmail = email.trim().toLowerCase();
        logger.info("[LOGIN] Attempting login for: {}", normalizedEmail);
        
        Optional<AppUser> found = userRepository.findByEmail(normalizedEmail);
        if (found.isEmpty()) {
            logger.warn("[LOGIN] No user found with email: {}", normalizedEmail);
            return null;  // No account with this email
        }
        AppUser user = found.get();
        logger.info("[LOGIN] User found - Email: {}, Role: {}", user.getEmail(), user.getRole());
        
        // Check if password hash is null
        if (user.getPasswordHash() == null) {
            logger.error("[LOGIN] Password hash is NULL for user: {}", user.getEmail());
            return null;
        }
        
        // BCrypt check — compares raw password to stored hash
        boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPasswordHash());
        logger.info("[LOGIN] Password verification result: {}", passwordMatches);
        
        if (passwordMatches) {
            logger.info("[LOGIN] ✓ Successful login for: {}", user.getEmail());
            return user;  // Valid credentials
        }
        logger.warn("[LOGIN] ✗ Password mismatch for user: {}", user.getEmail());
        return null;  // Wrong password
    }

    // ============================================================
    // FIND BY ID — used to reload user info from session userId
    // ============================================================
    public AppUser findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // ============================================================
    // ADMIN SEED
    // Called once on startup (from AdminSeeder) if env vars are set.
    // Creates the first Admin account from environment variables.
    // If an Admin already exists, does nothing (idempotent).
    // ============================================================
    public void seedAdminIfNotExists(String fullName, String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return;  // Env vars not set — skip silently
        }
        // Only create if no admin exists yet
        if (userRepository.existsByRole("ADMIN")) {
            logger.info("[ADMIN_SEED] Admin already exists, skipping seed");
            return;  // Admin already exists — do nothing
        }
        // Also skip if this email is already registered (e.g. re-seeded)
        if (userRepository.findByEmail(email.trim().toLowerCase()).isPresent()) {
            logger.info("[ADMIN_SEED] Email already exists, skipping seed");
            return;
        }

        AppUser admin = new AppUser();
        admin.setFullName(fullName != null && !fullName.isBlank() ? fullName.trim() : "System Admin");
        admin.setEmail(email.trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setRole("ADMIN");

        userRepository.save(admin);
        logger.info("[ADMIN_SEED] Admin account created: {}", admin.getEmail());
    }
}

