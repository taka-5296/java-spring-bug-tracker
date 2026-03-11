package com.example.bug_tracker.bug.repository;

import com.example.bug_tracker.bug.entity.BugEntity; // BugのDBエンティティ
import com.example.bug_tracker.bug.domain.BugStatus; // findByStatus用
import com.example.bug_tracker.bug.domain.BugPriority; // findByPriority用

import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPAの基本Repository

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// BugEntityをDB操作するRepository（CRUDの入口）
public interface BugRepository extends JpaRepository<BugEntity, Long>, BugRepositoryCustom {
    // JpaRepositoryがfindById/save等を提供する

    // status絞り込み + ページング
    Page<BugEntity> findByStatus(BugStatus status, Pageable pageable);

    // priority絞り込み + ページング
    Page<BugEntity> findByPriority(BugPriority priority, Pageable pageable);

    // status & priority絞り込み + ページング
    Page<BugEntity> findByStatusAndPriority(BugStatus status, BugPriority priority, Pageable pageable);

}