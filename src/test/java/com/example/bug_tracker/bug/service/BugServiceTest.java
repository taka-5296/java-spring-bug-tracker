package com.example.bug_tracker.bug.service;

// static import 
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

// JUnit5の@Testを使う / MockitoとJUnit5を連携させる
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Mockitoのモック機能を使う
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.entity.BugEntity;
import com.example.bug_tracker.bug.exception.BugNotFoundException;
import com.example.bug_tracker.bug.repository.BugRepository;

/**
 * BugService の単体テスト。
 * BugRepository をモックで置き換える。
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

        // Bug作成(create)正常系: status / priority = null の自動補完
        @Test
        void create_should_set_open_low_when_null() {
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

        // findAll 正常系: keywordの正規化、search()への委譲
        @Test
        void findAll_should_delegate_to_search_and_return_page() {
                // Arrange
                Pageable pageable = PageRequest.of(0, 10);

                BugEntity bug1 = new BugEntity(
                                "title1 keyword",
                                "description1",
                                BugStatus.OPEN,
                                BugPriority.LOW);
                bug1.setId(1L);

                Page<BugEntity> expectedPage = new PageImpl<>(List.of(bug1), pageable, 1);

                when(bugRepository.search(BugStatus.OPEN, null, "keyword", pageable))
                                .thenReturn(expectedPage);

                // Act
                Page<BugEntity> result = bugService.findAll(
                                BugStatus.OPEN,
                                null,
                                " keyword ", // 前後のスペースの正規化を確認
                                pageable);

                // Assert
                verify(bugRepository).search(BugStatus.OPEN, null, "keyword", pageable);
                assertThat(result.getTotalElements()).isEqualTo(1);
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
                assertThat(result.getContent().get(0).getTitle()).isEqualTo("title1 keyword");
                assertThat(result.getContent().get(0).getDescription()).isEqualTo("description1");
                assertThat(result.getContent().get(0).getStatus()).isEqualTo(BugStatus.OPEN);
                assertThat(result.getContent().get(0).getPriority()).isEqualTo(BugPriority.LOW);

        }

        // findAll 正常系: keywordの

        // findById 正常系
        @Test
        void findById_should_return_entity() {
                // Arrange
                Long bugId = 1L;
                BugEntity existing = new BugEntity(
                                "found title",
                                "found description",
                                BugStatus.OPEN,
                                BugPriority.LOW);
                existing.setId(bugId);

                when(bugRepository.findById(bugId)).thenReturn(Optional.of(existing));

                // Act
                BugEntity result = bugService.findById(bugId);

                // Assert
                verify(bugRepository).findById(bugId);
                assertThat(result.getId()).isEqualTo(bugId);
                assertThat(result.getTitle()).isEqualTo("found title");
                assertThat(result.getDescription()).isEqualTo("found description");
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

        // Bug更新(update)正常系
        @Test
        void updateById_should_update_fields() {
                // Arrange
                // 既存Bugを用意
                Long bugId = 1L;
                BugEntity existing = new BugEntity(
                                "old title",
                                "old description",
                                BugStatus.OPEN,
                                BugPriority.LOW);
                existing.setId(bugId);

                // save()後に返るEntityを用意
                BugEntity saved = new BugEntity(
                                "updated title",
                                "updated description",
                                BugStatus.DONE,
                                BugPriority.HIGH);
                saved.setId(bugId);

                when(bugRepository.findById(bugId)).thenReturn(Optional.of(existing));
                when(bugRepository.save(any(BugEntity.class))).thenReturn(saved);

                // Act

                BugEntity result = bugService.updateById(
                                bugId,
                                "updated title",
                                "updated description",
                                BugStatus.DONE,
                                BugPriority.HIGH);

                // Assert
                // 検証用の captor を用意
                ArgumentCaptor<BugEntity> captor = ArgumentCaptor.forClass(BugEntity.class);

                // findById() と save() が呼ばれたことを確認する
                verify(bugRepository).findById(bugId);
                verify(bugRepository).save(captor.capture());

                BugEntity actualSavedArg = captor.getValue();

                // save() に渡された Entity を確認する
                assertThat(actualSavedArg.getId()).isEqualTo(bugId); // IDは変わらない
                assertThat(actualSavedArg.getTitle()).isEqualTo("updated title");
                assertThat(actualSavedArg.getDescription()).isEqualTo("updated description");
                assertThat(actualSavedArg.getStatus()).isEqualTo(BugStatus.DONE);
                assertThat(actualSavedArg.getPriority()).isEqualTo(BugPriority.HIGH);

                // 戻り値もRepositoryのsave結果と一致するか確認する
                assertThat(result.getId()).isEqualTo(bugId);
                assertThat(result.getTitle()).isEqualTo("updated title");
                assertThat(result.getDescription()).isEqualTo("updated description");
                assertThat(result.getStatus()).isEqualTo(BugStatus.DONE);
                assertThat(result.getPriority()).isEqualTo(BugPriority.HIGH);
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

        // deleteById 正常系
        @Test
        void deletedById_should_delete_bug() {
                // Arrange
                // 既存bugを用意
                Long bugId = 1L;
                BugEntity existing = new BugEntity(
                                "delte target",
                                "to be deleted",
                                BugStatus.OPEN,
                                BugPriority.LOW);
                existing.setId(bugId);

                when(bugRepository.findById(bugId)).thenReturn(Optional.of(existing));

                // Act

                bugService.deleteById(bugId);

                // Assert
                // findById() と delete() が呼ばれたことを確認する
                verify(bugRepository).findById(bugId);
                verify(bugRepository).delete(existing);
                verify(bugRepository, never()).save(any(BugEntity.class));
        }

        // deleteById 異常系 404 Not Found
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