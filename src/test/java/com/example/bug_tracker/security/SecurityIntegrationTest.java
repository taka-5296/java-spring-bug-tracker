package com.example.bug_tracker.security;

import com.example.bug_tracker.bug.service.BugService;
import com.example.bug_tracker.user.domain.UserRole;
import com.example.bug_tracker.user.entity.UserEntity;
import com.example.bug_tracker.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties.Pageable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 公開・未認証・login・USER・ADMIN・CSRF・DB認証 のテスト
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private BugService bugService;

    @BeforeEach
    void setUpUsers() {

        userRepository.deleteAll();

        userRepository.save(
                new UserEntity(
                        "security-user",
                        passwordEncoder.encode("user-password"),
                        UserRole.USER,
                        true));

        userRepository.save(
                new UserEntity(
                        "security-admin",
                        passwordEncoder.encode("admin-password"),
                        UserRole.ADMIN,
                        true));
    }

    // healthは公開
    @Test
    void health_should_be_public() throws Exception {

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    // リダイレクトチェック
    @Test
    void bugs_should_redirect_to_login_when_unauthenticated() throws Exception {

        mockMvc.perform(get("/api/bugs"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));

    }

    // userログイン成功
    @Test
    void login_should_authenticate_db_user() throws Exception {

        mockMvc.perform(
                formLogin()
                        .user("security-user")
                        .password("user-password"))
                .andExpect(
                        authenticated()
                                .withUsername("security-user")
                                .withRoles("USER"));
    }

    // パスワードミスによるログイン失敗
    @Test
    void login_should_reject_wrong_password() throws Exception {

        mockMvc.perform(
                formLogin()
                        .user("security-user")
                        .password("wrong-password"))
                .andExpect(unauthenticated());
    }

    // usernameミスによるログイン失敗
    @Test
    void login_should_reject_unknown_username() throws Exception {

        mockMvc.perform(
                formLogin()
                        .user("unknown-user")
                        .password("user-password"))
                .andExpect(unauthenticated());
    }

    // user認証後の(GET /api/bugs)アクセス
    @Test
    void user_should_access_bug_api() throws Exception {

        var pageable = PageRequest.of(0, 10);

        when(bugService.findAll(null, null, null, pageable))
                .thenReturn(Page.empty(pageable));

        mockMvc.perform(
                get("/api/bugs")
                        .with(user("security-user").roles("USER")))
                .andExpect(status().isOk());
    }

    // user、DELETE不可(403)
    @Test
    void user_should_get_403_when_deleting_bug() throws Exception {

        mockMvc.perform(
                delete("/api/bugs/1")
                        .with(user("security-user").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        // 認可で止まったか確認
        verifyNoInteractions(bugService);
    }

    // ADMIN、DELETEテスト
    @Test
    void admin_should_delete_bug_with_csrf() throws Exception {

        mockMvc.perform(
                delete("/api/bugs/1")
                        .with(user("security-admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(bugService).deleteById(1L);
    }

    // ADMINでCSRF認証なし、DELETE不可(403)
    @Test
    void admin_should_get_403_without_csrf() throws Exception {

        mockMvc.perform(
                delete("/api/bugs/1")
                        .with(user("security-admin").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bugService);
    }

}
