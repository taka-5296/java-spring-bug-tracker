package com.example.bug_tracker.bug.repository;

import com.example.bug_tracker.bug.entity.BugEntity; // BugのDBエンティティ
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPAの基本Repository

// BugEntityをDB操作するRepository（CRUDの入口）
public interface BugRepository extends JpaRepository<BugEntity, Long> {
    // JpaRepositoryがfindById/save等を提供する（ここは基本追加不要）
}