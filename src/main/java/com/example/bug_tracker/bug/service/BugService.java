package com.example.bug_tracker.bug.service;

import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.entity.BugEntity;
import com.example.bug_tracker.bug.exception.BugNotFoundException;
import com.example.bug_tracker.bug.repository.BugRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BugService {
    private static final Logger log = LoggerFactory.getLogger(BugService.class);

    private final BugRepository bugRepository;

    public BugService(BugRepository bugRepository) {
        this.bugRepository = bugRepository;
    }

    @Transactional
    public BugEntity create(String title, String description, BugStatus status, BugPriority priority) {
        log.info("BugService#create called");

        BugStatus fixedStatus = (status != null) ? status : BugStatus.OPEN;
        BugPriority fixedPriority = (priority != null) ? priority : BugPriority.LOW;

        BugEntity entity = new BugEntity(title, description, fixedStatus, fixedPriority);
        return bugRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<BugEntity> findAll() {
        log.info("BugService#findAll called");
        // ソートは暫定：id昇順（必要なら後で変更）
        return bugRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BugEntity findById(long id) {
        log.info("BugService#findById called. id={}", id);
        return bugRepository.findById(id)
                .orElseThrow(() -> new BugNotFoundException(id));
    }
}