package com.example.bug_tracker.bug.service;

import com.example.bug_tracker.bug.repository.BugRepository;
import com.example.bug_tracker.bug.entity.BugEntity;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.domain.BugPriority;

// JUnit5の@Testを使う / MockitoとJUnit5を連携させる
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

// Mockitoのモック機能を使う
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

// static import 
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * BugService の単体テスト。
 * Spring は起動せず、Repository は Mockito のモックで置き換える。
 */

// JUnit5 実行時に Mockito の @Mock / @InjectMocks を有効化する
@ExtendWith(MockitoExtension.class)
public class BugServiceTest {

    // Repositoryをモック化
    @Mock
    private BugRepository bugRepository;

    // Mockを注入したBugService（テスト対象）を作る
    @InjectMocks
    private BugService bugService;

    @Test
    void should_set_open_and_low_when_null() {
        // Arrange: ave() が呼ばれたときに返す「保存後の Entity」を準備する
        BugEntity savedEntity = new BugEntity(
                "login error",
                "created in test",
                BugStatus.OPEN,
                BugPriority.LOW);
        savedEntity.setId(1L);

        when(bugRepository.save(org.mockito.ArgumentMatchers.any(BugEntity.class)))
                .thenReturn(savedEntity);

        // Act: status / priority を null で渡して create を実行する
        BugEntity result = bugService.create(
                "login error",
                "created in test",
                null,
                null);

        // Assert: saveに渡されたEntityの中身を検証するため captor を用意
        ArgumentCaptor<BugEntity> captor = ArgumentCaptor.forClass(BugEntity.class);

        // save()が一階呼ばれたことを確認し、captor に記録
        verify(bugRepository, times(1)).save(captor.capture());

        BugEntity actualSavedArg = captor.getValue();

        // save() に渡された Entity の内容が期待通りか確認する
        assertThat(actualSavedArg.getTitle()).isEqualTo("login error");
        assertThat(actualSavedArg.getDescription()).isEqualTo("created in test");
        assertThat(actualSavedArg.getStatus()).isEqualTo(BugStatus.OPEN);
        assertThat(actualSavedArg.getPriority()).isEqualTo(BugPriority.LOW);

        // 戻り値もRepositoryのsave結果になっていることを確認する
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BugStatus.OPEN);
        assertThat(result.getPriority()).isEqualTo(BugPriority.LOW);
    }
}