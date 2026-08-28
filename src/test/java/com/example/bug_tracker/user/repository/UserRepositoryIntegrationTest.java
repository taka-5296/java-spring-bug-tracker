package com.example.bug_tracker.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.bug_tracker.user.domain.UserRole;
import com.example.bug_tracker.user.entity.UserEntity;

import jakarta.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_should_return_user() {

        UserEntity savedTestUser = userRepository.save(
                new UserEntity(
                        "repository-test-user",
                        "test-hash",
                        UserRole.USER,
                        true));

        var result = userRepository.findByUsername("repository-test-user");

        assertThat(result).isPresent(); // 存在の検証
        assertThat(result.get().getId()).isEqualTo(savedTestUser.getId());
        assertThat(result.get().getUsername()).isEqualTo("repository-test-user");
        assertThat(result.get().getRole()).isEqualTo(UserRole.USER);
        assertThat(result.get().isEnabled()).isTrue();
    }
}
