package com.fsms.config;

import com.fsms.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// ============================================================
// AdminSeeder runs once when the application starts.
// It reads three environment variables:
//   ADMIN_FULL_NAME  — display name of the admin
//   ADMIN_EMAIL      — email used to log in as admin
//   ADMIN_PASSWORD   — password (will be BCrypt-hashed before saving)
//
// If any of these are missing, or if an Admin already exists,
// this method does nothing. It is safe to run on every startup.
//
// NEVER hardcode credentials here. Set env vars in run.ps1 locally.
// ============================================================
@Component
public class AdminSeeder implements ApplicationRunner {

    private final UserService userService;

    public AdminSeeder(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        String fullName = System.getenv("ADMIN_FULL_NAME");
        String email    = System.getenv("ADMIN_EMAIL");
        String password = System.getenv("ADMIN_PASSWORD");

        // Delegate to UserService — only creates admin if none exists yet
        userService.seedAdminIfNotExists(fullName, email, password);
    }
}
