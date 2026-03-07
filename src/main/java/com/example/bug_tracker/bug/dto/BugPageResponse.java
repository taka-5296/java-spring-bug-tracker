package com.example.bug_tracker.bug.dto;

import java.util.List;

public record BugPageResponse(
        List<BugResponse> items,
        PageMetaResponse meta) {

}
