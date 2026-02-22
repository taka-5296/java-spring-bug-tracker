package com.example.bug_tracker.bug.api;

import com.example.bug_tracker.bug.domain.Bug;
import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.service.BugService;
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

    // DTO（入力用）
    public record CreateBugRequest(
            String title,
            String description,
            BugStatus status,
            BugPriority priority) {
    }

    @PostMapping
    public Bug create(@RequestBody CreateBugRequest req) {
        log.info("BugController#create called");
        return bugService.create(req.title(), req.description(), req.status(), req.priority());
    }

    @GetMapping
    public List<Bug> list() {
        log.info("BugController#list called");
        return bugService.findAll();
    }
}