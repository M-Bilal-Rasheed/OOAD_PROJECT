package com.fsms.repository;

import com.fsms.model.Comparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository for saved comparisons.
@Repository
public interface ComparisonRepository extends JpaRepository<Comparison, Long> {

    // Get all comparisons saved by a specific user, newest first
    List<Comparison> findByUserIdOrderByCreatedAtDesc(Long userId);
}
