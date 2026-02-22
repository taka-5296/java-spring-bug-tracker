package com.example.bug_tracker.bug.domain;

import java.time.Instant;

public class Bug {
    private final long id;
    private final String title;
    private final String description;
    private final BugStatus status;
    private final BugPriority priority;
    private final Instant createdAt;

    public Bug(long id, String title, String description, BugStatus status, BugPriority priority, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BugStatus getStatus() {
        return status;
    }

    public BugPriority getPriority() {
        return priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
