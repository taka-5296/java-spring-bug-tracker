package com.example.bug_tracker.bug.service;

// static import
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.entity.BugEntity;
import com.example.bug_tracker.bug.repository.BugRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BugServiceIntegrationTest {

    @Autowired
    private BugService bugService;

    @Autowired
    private BugRepository bugRepository;

    @Autowired
    private EntityManager entityManager;

    // Service + Repository + DB の結合テスト
    @Test
    void create_then_findById_should_persist_and_load_bug_via_repository_and_db() {

        // Arrange テスト用Bugを準備
        String title = "integration test bug";
        String description = "saved to db";
        BugStatus status = BugStatus.OPEN;
        BugPriority priority = BugPriority.HIGH;

        // Act 1: Service経由でBugの保存
        BugEntity created = bugService.create(title, description, status, priority);

        // Assert 1: IDENTITYで採番されたDBのIDを確認
        assertThat(created.getId()).isNotNull();

        // 永続化コンテキストで fulsh / clear を明示し、SQLの強制発行および、メモリの削除を行う。
        entityManager.flush();
        entityManager.clear();

        // Act 2: Service経由でIDの再取得
        BugEntity loaded = bugService.findById(created.getId());

        // Assert 2: Repository / DB を通した結果が一致するか確認
        // createdとの比較
        assertThat(loaded.getId()).isEqualTo(created.getId());
        assertThat(created.getTitle()).isEqualTo(title);
        assertThat(created.getDescription()).isEqualTo(description);
        assertThat(created.getStatus()).isEqualTo(status);
        assertThat(created.getPriority()).isEqualTo(priority);
        // loadedとの比較
        assertThat(loaded.getTitle()).isEqualTo(title);
        assertThat(loaded.getDescription()).isEqualTo(description);
        assertThat(loaded.getStatus()).isEqualTo(status);
        assertThat(loaded.getPriority()).isEqualTo(priority);

        // 参考確認: Repository直読みでも存在する
        assertThat(bugRepository.findById(created.getId())).isPresent();
    }
}
