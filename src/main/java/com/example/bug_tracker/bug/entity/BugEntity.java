package com.example.bug_tracker.bug.entity;

import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.domain.BugPriority;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "bugs") // bugsテーブルのEntity
public class BugEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BugStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BugPriority priority;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected BugEntity() {
        // JPA用
    }

    public BugEntity(String title, String description, BugStatus status, BugPriority priority) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
    }

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null)
            createdAt = now;
        if (updatedAt == null)
            updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // 以下getter
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BugStatus getStatus() {
        return status;
    }

    public BugPriority getPriority() {
        return priority;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    // 以下setter

    public void setId(Long id) {
        this.id = id; // IDを設定（通常はDBが自動生成する）
    }

    public void setTitle(String title) {
        this.title = title; // タイトルを更新
    }

    public void setDescription(String description) {
        this.description = description; // 説明を更新
    }

    public void setStatus(BugStatus status) {
        this.status = status; // ステータスを更新（型一致）
    }

    public void setPriority(BugPriority priority) {
        this.priority = priority; // 優先度を更新（型一致）
    }
}