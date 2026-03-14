package com.example.bug_tracker.bug.service;

// static import 
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

// JUnit5の@Testを使う / MockitoとJUnit5を連携させる
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Mockitoのモック機能を使う
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.entity.BugEntity;
import com.example.bug_tracker.bug.exception.BugNotFoundException;
import com.example.bug_tracker.bug.repository.BugRepository;

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

        // create 正常系: status / priority = null の自動補完
        @Test
        void should_set_open_and_low_when_null() {
                // Arrange: save() が呼ばれたときに返す「保存後の Entity」を準備する
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

                // save()が一回呼ばれたことを確認し、captor に記録
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

        // findById 異常系 404 Not Found
        @Test
        void findById_should_throw_not_found() {
                // Arrange: findById() が呼ばれたときに Optional.empty() を返す
                long missingId = 999L;
                when(bugRepository.findById(missingId)).thenReturn(Optional.empty());

                // Act
                BugNotFoundException ex = assertThrows(
                                BugNotFoundException.class, () -> bugService.findById(missingId));

                // Assert: findById() が呼ばれたときに例外が正常に投げられることを確認する
                verify(bugRepository).findById(missingId);
                assertThat(ex.getId()).isEqualTo(missingId);
                assertThat(ex.getMessage()).isEqualTo("Bug not found. id =" + missingId);
        }

        // updateById 異常系 404 Not Found
        @Test
        void updateById_should_throw_not_found() {
                // Arrange: updateById() が呼ばれたときにOptional.empty() を返す
                long missingId = 999L;
                when(bugRepository.findById(missingId)).thenReturn(Optional.empty());

                // Act
                BugNotFoundException ex = assertThrows(
                                BugNotFoundException.class, () -> bugService.updateById(
                                                missingId,
                                                "updated title",
                                                "updated description",
                                                BugStatus.DONE,
                                                BugPriority.HIGH));

                // Assert: updateById() が呼ばれたときに例外が正常に投げられることを確認する
                verify(bugRepository).findById(missingId);
                verify(bugRepository, never()).save(any(BugEntity.class));

                assertThat(ex.getId()).isEqualTo(missingId);
                assertThat(ex.getMessage()).isEqualTo("Bug not found. id =" + missingId);
        }

        @Test
        void deleteById_should_throw_not_found() {
                // Arrange: findById() が呼ばれたときに Optional.empty() を返す
                long missingId = 999L;
                when(bugRepository.findById(missingId)).thenReturn(Optional.empty());

                // Act
                BugNotFoundException ex = assertThrows(
                                BugNotFoundException.class, () -> bugService.deleteById(missingId));

                // Assert: deleteById() が呼ばれたときに例外が正常に投げられることを確認する
                verify(bugRepository).findById(missingId);
                verify(bugRepository, never()).delete(any(BugEntity.class));

                assertThat(ex.getId()).isEqualTo(missingId);
                assertThat(ex.getMessage()).isEqualTo("Bug not found. id =" + missingId);
        }

}