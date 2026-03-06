package com.example.bug_tracker.bug.repository;

import com.example.bug_tracker.bug.entity.BugEntity; // BugのDBエンティティ
import com.example.bug_tracker.bug.domain.BugStatus; // findByStatus用

import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPAの基本Repository

import java.util.List;

// BugEntityをDB操作するRepository（CRUDの入口）
public interface BugRepository extends JpaRepository<BugEntity, Long> {
    // JpaRepositoryがfindById/save等を提供する

    //
    List<BugEntity> findByStatus(BugStatus status);

}