package com.example.bug_tracker.bug.repository;

import com.example.bug_tracker.bug.entity.BugEntity; // BugのDBエンティティ

import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPAの基本Repository

public interface BugRepository extends JpaRepository<BugEntity, Long>, BugRepositoryCustom {

}