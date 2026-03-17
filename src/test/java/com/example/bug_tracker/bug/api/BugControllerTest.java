package com.example.bug_tracker.bug.api;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import com.example.bug_tracker.bug.domain.BugPriority;
import com.example.bug_tracker.bug.domain.BugStatus;
import com.example.bug_tracker.bug.entity.BugEntity;
import com.example.bug_tracker.bug.service.BugService;
import com.example.bug_tracker.common.api.GlobalExceptionHandler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BugController.class)
@Import(GlobalExceptionHandler.class)
class BugControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BugService bugService;

  @Test
  void create_should_return_201_and_response_body() throws Exception {
    BugEntity saved = new BugEntity(
        "controller test title",
        "controller test description",
        BugStatus.OPEN,
        BugPriority.MEDIUM);
    saved.setId(1L);

    when(bugService.create(
        "controller test title",
        "controller test description",
        BugStatus.OPEN,
        BugPriority.MEDIUM))
        .thenReturn(saved);

    String requestBody = """
        {
          "title": "controller test title",
          "description": "controller test description",
          "status": "OPEN",
          "priority": "MEDIUM"
        }
        """;

    mockMvc.perform(post("/api/bugs")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/api/bugs/1"))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("controller test title"))
        .andExpect(jsonPath("$.description").value("controller test description"))
        .andExpect(jsonPath("$.status").value("OPEN"))
        .andExpect(jsonPath("$.priority").value("MEDIUM"));

    verify(bugService).create(
        "controller test title",
        "controller test description",
        BugStatus.OPEN,
        BugPriority.MEDIUM);
  }

  @Test
  void create_should_return_400_when_title_is_blank() throws Exception {
    String requestBody = """
        {
          "title": "",
          "description": "invalid request",
          "status": "OPEN",
          "priority": "LOW"
        }
        """;

    mockMvc.perform(post("/api/bugs")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("入力値が不正です"))
        .andExpect(jsonPath("$.details").isArray());
  }
}