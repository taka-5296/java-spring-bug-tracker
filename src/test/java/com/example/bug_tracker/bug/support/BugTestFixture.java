package com.example.bug_tracker.bug.support;

import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.entity.BugEntity;

public final class BugTestFixture {

    private BugTestFixture() {
    }

    public static BugEntity bug(
            String title,
            String description,
            BugStatus status,
            BugPriority priority) {

        return new BugEntity(title, description, status, priority);
    }

    public static BugEntity bugWithId(
            Long id,
            String title,
            String description,
            BugStatus status,
            BugPriority priority) {

        BugEntity entity = new BugEntity(title, description, status, priority);
        entity.setId(id);
        return entity;
    }

    public static String createRequestJson(
            String title,
            String description,
            BugStatus status,
            BugPriority priority) {

        String statusValue = (status == null) ? "null" : "\"" + status.name() + "\"";
        String priorityValue = (priority == null) ? "null" : "\"" + priority.name() + "\"";

        return """
                {
                  "title": "%s",
                  "description": "%s",
                  "status": %s,
                  "priority": %s
                }
                """.formatted(
                escapeJson(title),
                escapeJson(description),
                statusValue,
                priorityValue);
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}