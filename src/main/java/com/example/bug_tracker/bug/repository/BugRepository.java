package com.example.bug_tracker.bug.repository;

import com.example.bug_tracker.bug.entity.BugEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BugRepository extends JpaRepository<BugEntity, Long> {
}