package com.example.bug_tracker.bug.dto;

import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;

import java.time.OffsetDateTime;

public record BugResponse(
        Long id,
        String title,
        String description,
        BugStatus status,
        BugPriority priority,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}