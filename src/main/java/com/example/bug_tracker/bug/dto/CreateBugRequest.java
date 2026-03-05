package com.example.bug_tracker.bug.dto;

import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;

import jakarta.validation.constraints.NotBlank;

// 入力用DTO
public record CreateBugRequest(
                @NotBlank(message = "title must not be blank") // titleは必須
                String title,
                String description,
                BugStatus status,
                BugPriority priority) {

}
