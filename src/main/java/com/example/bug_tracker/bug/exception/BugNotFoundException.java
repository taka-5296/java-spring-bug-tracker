package com.example.bug_tracker.bug.exception;

public class BugNotFoundException extends RuntimeException {
    private final long id;

    public BugNotFoundException(long id) {
        super("Bug not found. id =" + id);
        this.id = id;
    }

    public long getId() {
        return id;
    }

}