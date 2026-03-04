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

    @Transactional
    public BugEntity updateById(long id, String title, String description, BugStatus status, BugPriority priority) {
        log.info("BugService#updateById called. id={}", id); // 更新開始ログ

        // 1) 既存を取得（無ければNotFound＝404）
        BugEntity entity = bugRepository.findById(id) // Optional<BugEntity>で返る
                .orElseThrow(() -> new BugNotFoundException(id)); // 「見つからない」をServiceで確定

        // 2) null補完（Createと同じ方針で固定）
        BugStatus fixedStatus = (status != null) ? status : BugStatus.OPEN; // nullならOPEN
        BugPriority fixedPriority = (priority != null) ? priority : BugPriority.LOW; // nullならLOW

        // 3) Entityへ反映（ここが“DBに保存される内容”）
        entity.setTitle(title); // タイトル更新
        entity.setDescription(description); // 説明更新（null許容ならそのまま）
        entity.setStatus(fixedStatus); // ステータス更新
        entity.setPriority(fixedPriority); // 優先度更新

        // 4) 保存（JPAがUPDATEを発行）
        return bugRepository.save(entity);
    }

    // 削除(DELETE)
    @Transactional
    public void deleteById(long id) {
        log.info("BugService#deleteById called. id={}", id); // 削除開始ログ

        // 1) 既存を取得(無ければNotFound=404)
        BugEntity entity = bugRepository.findById(id)
                .orElseThrow(() -> new BugNotFoundException(id));

        // 2) 指定Bug削除(PAがDELTEを発行)
        bugRepository.delete(entity);

        log.info("BugService#deleteById succeeded. id={}", id); // 削除成功ログ
    }
}