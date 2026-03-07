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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // status 指定は絞り込み、無ければ全件
    @Transactional(readOnly = true)
    public Page<BugEntity> findAll(BugStatus status, Pageable pageable) {

        if (status == null) {
            // ログ-filterなし
            log.info("BugService#findAll called. no status filter");
            return bugRepository.findAll(pageable);
        }

        // ログ-filterあり
        log.info("BugService#findAll called. status={}, page={}, size={}", status, pageable.getPageNumber(),
                pageable.getPageSize());

        // (status != null)でBugRepository固有メソッド呼び出し
        return bugRepository.findByStatus(status, pageable);
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

        // 3) Entityへ反映
        entity.setTitle(title); // タイトル更新
        entity.setDescription(description); // 説明更新
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