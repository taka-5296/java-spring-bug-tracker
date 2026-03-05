package com.example.bug_tracker.bug.api;

import com.example.bug_tracker.bug.dto.BugResponse;
import com.example.bug_tracker.bug.entity.BugEntity;
import com.example.bug_tracker.bug.service.BugService;
import com.example.bug_tracker.bug.dto.CreateBugRequest;
import com.example.bug_tracker.bug.dto.UpdateBugRequest;

import jakarta.validation.Valid; // Validation
import org.springframework.http.ResponseEntity; //DELETE用

import org.slf4j.Logger; // Log
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*; // アノテーション
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController // コントローラー宣言
@RequestMapping("/api/bugs")
public class BugController {
    private static final Logger log = LoggerFactory.getLogger(BugController.class);

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    // Bug新規登録(POST) 成功で201 createdに固定
    @PostMapping
    public ResponseEntity<BugResponse> create(@Valid @RequestBody CreateBugRequest req) {
        log.info("BugController#create called");

        BugEntity saved = bugService.create(req.title(), req.description(), req.status(), req.priority());
        BugResponse body = toResponse(saved);

        // Location: /api/bugs/{id}
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(body);
    }

    // Bug一覧取得(GET)
    @GetMapping
    public List<BugResponse> list() {
        log.info("BugController#list called");
        return bugService.findAll().stream().map(this::toResponse).toList();
    }

    // Bug個別取得(GET)
    @GetMapping("/{id}")
    public BugResponse getById(@PathVariable long id) {
        log.info("BugController#getById called. id={}", id);
        return toResponse(bugService.findById(id));
    }

    // Bug更新(PUT)
    @PutMapping("/{id}")
    public BugResponse update(@PathVariable long id, @Valid @RequestBody UpdateBugRequest req) {
        log.info("BugController#update called. id={}", id);
        BugEntity updated = bugService.updateById(id, req.title(), req.description(), req.status(), req.priority());
        return toResponse(updated);
    }

    // Bug削除(id指定)(DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        log.info("BugController#deleteById called. id={}", id); // ログ開始

        bugService.deleteById(id); // Service呼び出し

        return ResponseEntity.noContent().build(); // ボディ無しレスポンスをビルド(204)
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