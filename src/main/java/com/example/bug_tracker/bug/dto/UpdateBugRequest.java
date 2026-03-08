package com.example.bug_tracker.bug.dto;

import com.example.bug_tracker.bug.domain.BugPriority; // priority enum
import com.example.bug_tracker.bug.domain.BugStatus; // status enum

import jakarta.validation.constraints.NotBlank; // title必須
import jakarta.validation.constraints.Size; // 文字数制限（任意）

//　更新用DTO(全置換）
public record UpdateBugRequest(
                @NotBlank(message = "title must not be blank") // titleは必須
                @Size(max = 200) // VARCHAR(200)
                String title, // 更新後タイトル
                String description, // 更新後説明（任意）
                BugStatus status, // 更新後ステータス（nullなら補完）
                BugPriority priority // 更新後優先度（nullなら補完）
) {
}