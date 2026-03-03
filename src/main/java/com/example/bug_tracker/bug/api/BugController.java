package com.example.bug_tracker.bug.api;

import com.example.bug_tracker.bug.dto.BugResponse;
import com.example.bug_tracker.bug.entity.BugEntity;
import com.example.bug_tracker.bug.service.BugService;
import com.example.bug_tracker.bug.dto.CreateBugRequest;
import com.example.bug_tracker.bug.dto.UpdateBugRequest;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bugs")
public class BugController {
    private static final Logger log = LoggerFactory.getLogger(BugController.class);

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @PostMapping
    public BugResponse create(@Valid @RequestBody CreateBugRequest req) {
        log.info("BugController#create called");
        BugEntity saved = bugService.create(req.title(), req.description(), req.status(), req.priority());
        return toResponse(saved);
    }

    @GetMapping
    public List<BugResponse> list() {
        log.info("BugController#list called");
        return bugService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public BugResponse getById(@PathVariable long id) {
        log.info("BugController#getById called. id={}", id);
        return toResponse(bugService.findById(id));
    }

    @PutMapping("/{id}")
    public BugResponse update(@PathVariable long id, @Valid @RequestBody UpdateBugRequest req) {
        log.info("BugController#update called. id={}", id);
        BugEntity updated = bugService.updateById(id, req.title(), req.description(), req.status(), req.priority());
        return toResponse(updated);
    }

    // 返却DTOの値取得
    private BugResponse toResponse(BugEntity e) {
        return new BugResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStatus(),
                e.getPriority(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }
}