package com.example.bug_tracker.bug.service;

import com.example.bug_tracker.bug.domain.Bug;
import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.exception.BugNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BugService {
    private static final Logger log = LoggerFactory.getLogger(BugService.class);

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final List<Bug> store = new ArrayList<>();

    public Bug create(String title, String description, BugStatus status, BugPriority priority) {
        log.info("BugService#create called");

        // ★追加：サーバ側デフォルト補完
        BugStatus fixedStatus = (status != null) ? status : BugStatus.OPEN;
        BugPriority fixedPriority = (priority != null) ? priority : BugPriority.LOW;

        long id = idGenerator.getAndIncrement();
        Bug bug = new Bug(id, title, description, fixedStatus, fixedPriority, Instant.now());
        store.add(bug);
        return bug;
    }

    public List<Bug> findAll() {
        log.info("BugService#findAll called");
        return Collections.unmodifiableList(store);
    }

    public Bug findById(long id) {
        log.info("BugService#findById called. id={}", id);
        return store.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElseThrow(() -> new BugNotFoundException(id));
    }
}
