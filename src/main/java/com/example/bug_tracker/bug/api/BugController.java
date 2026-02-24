package com.example.bug_tracker.bug.api;

import com.example.bug_tracker.bug.domain.Bug;
import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.service.BugService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bugs")
public class BugController {
    private static final Logger log = LoggerFactory.getLogger(BugController.class);

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    // DTO（入力用）
    public record CreateBugRequest(
            @NotBlank(message = "title must not be blank") String title,
            String description,
            BugStatus status,
            BugPriority priority) {
    }

    @PostMapping
    public Bug create(@Valid @RequestBody CreateBugRequest req) {
        log.info("BugController#create called");
        return bugService.create(req.title(), req.description(), req.status(), req.priority());
    }

    @GetMapping
    public List<Bug> list() {
        log.info("BugController#list called");
        return bugService.findAll();
    }

    @GetMapping("/{id}")
    public Bug getById(@PathVariable long id) {
        log.info("BugController#getById called. id={}", id);
        return bugService.findById(id);
    }
}