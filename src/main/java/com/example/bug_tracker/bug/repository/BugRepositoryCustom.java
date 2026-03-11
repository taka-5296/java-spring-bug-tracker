package com.example.bug_tracker.bug.repository;

import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.entity.BugEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 動的検索専用の拡張repositoryインタフェース
public interface BugRepositoryCustom {

    // status / priority / keyword の複合検索用DTO
    Page<BugEntity> search(
            BugStatus status,
            BugPriority priority,
            String keyword,
            Pageable pageable);
}
