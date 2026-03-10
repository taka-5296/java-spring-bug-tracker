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

    // Bug作成 (POST)
    @Transactional
    public BugEntity create(String title, String description, BugStatus status, BugPriority priority) {
        log.info("BugService#create called");

        BugStatus fixedStatus = (status != null) ? status : BugStatus.OPEN;
        BugPriority fixedPriority = (priority != null) ? priority : BugPriority.LOW;

        BugEntity entity = new BugEntity(title, description, fixedStatus, fixedPriority);
        BugEntity result = bugRepository.save(entity);

        // Bug作成成功ログ
        log.info("BugService#create succeeded. saved id={}", result.getId());

        return result;
    }

    // status / priority / keyword で絞り込み or 全件取得 (GET)
    @Transactional(readOnly = true)
    public Page<BugEntity> findAll(
            BugStatus status,
            BugPriority priority,
            String keyword,
            Pageable pageable) {

        // keyword を正規化する
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        // 条件なし
        if (status == null && priority == null) {
            // ログ-filterなし
            log.info("BugService#findAll called. no filter, page={}, size={}",
                    pageable.getPageNumber(), pageable.getPageSize());

            // 一覧検索
            Page<BugEntity> result = bugRepository.findAll(pageable);

            // 一覧検索成功ログ
            log.info("BugService#findAll returnedElements={}", result.getNumberOfElements());

            return result;
        }

        // statusのみ
        if (status != null && priority == null) {

            // ログ-status filter
            log.info("BugService#findAll called. status={},  page={}, size={}", status, pageable.getPageNumber(),
                    pageable.getPageSize());

            // statusで絞り込み検索
            Page<BugEntity> result = bugRepository.findByStatus(status, pageable);

            // status絞り込み検索成功ログ(件数)
            log.info("BugService#findAll returnedElements={}", result.getNumberOfElements());

            return result;

        }

        // priorityのみ
        if (status == null) {

            // ログ-priority filter
            log.info("BugService#findAll called. priority={},  page={}, size={}", priority, pageable.getPageNumber(),
                    pageable.getPageSize());

            // priorityで絞込検索
            Page<BugEntity> result = bugRepository.findByPriority(priority, pageable);

            // priority絞り込み検索成功ログ(件数)
            log.info("BugService#findAll returnedElements={}", result.getNumberOfElements());

            return result;
        }

        // status & priority
        // ログ-status & priority filter
        log.info("BugService#findAll called. status={}, priority={},  page={}, size={}", status, priority,
                pageable.getPageNumber(),
                pageable.getPageSize());

        // (status != null && priority != null)で絞込検索
        Page<BugEntity> result = bugRepository.findByStatusAndPriority(status, priority,
                pageable);

        // status & priority絞り込み検索成功ログ(件数)
        log.info("BugService#findAll returnedElements={}", result.getNumberOfElements());

        return result;
    }

    @Transactional(readOnly = true)
    public BugEntity findById(long id) {
        log.info("BugService#findById called. id={}", id);

        // id指定検索（無ければNotFound）
        BugEntity result = bugRepository.findById(id)
                .orElseThrow(() -> new BugNotFoundException(id));

        // id指定検索成功ログ
        log.info("BugService#findById succeeded. id={}", id);

        return result;

    }

    // 更新(PUT)
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
        BugEntity result = bugRepository.save(entity);

        // 更新成功ログ
        log.info("BugService#updateById succeeded. id={}", result.getId());

        return result;
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