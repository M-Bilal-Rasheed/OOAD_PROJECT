package com.fsms.repository;

import com.fsms.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repository for AppUser — handles database access for user accounts.
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // Used during login and duplicate-email check during signup
    Optional<AppUser> findByEmail(String email);

    // Used during admin seeding to check whether an admin already exists
    boolean existsByRole(String role);
}
