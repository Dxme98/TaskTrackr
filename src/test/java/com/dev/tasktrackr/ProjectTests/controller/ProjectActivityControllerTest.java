package com.dev.tasktrackr.ProjectTests.controller;

import com.dev.tasktrackr.activity.ActivityType;
import com.dev.tasktrackr.activity.ProjectActivityController;
import com.dev.tasktrackr.activity.ProjectActivityDto;
import com.dev.tasktrackr.activity.ProjectActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProjectActivityController.class)
@DisplayName("ProjectActivityController (WebMvcTest)")
class ProjectActivityControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectActivityService projectActivityService;

    private static final String API_BASE_URL = "/api/v1";
    private String testUserId;
    private String testUsername;
    private Long testProjectId;

    @BeforeEach
    void setUp() {
        testUserId = "user-id-abc-123";
        testUsername = "test-activity-user";
        testProjectId = 1L;
    }

    @Nested
    @DisplayName("GET /projects/{projectId}/activities")
    class GetProjectActivitiesTests {

        @Test
        @DisplayName("Should return 200 OK and a page of activities")
        void getProjectActivities_whenActivitiesExist_shouldReturn200AndPage() throws Exception {
            // Given
            ProjectActivityDto activity1 = new ProjectActivityDto(1L, ActivityType.TASK_CREATED, "actor1", "Task A", null, Instant.now());
            ProjectActivityDto activity2 = new ProjectActivityDto(2L, ActivityType.USER_JOINED_PROJECT, "actor2", null, "hat das Projekt betreten", Instant.now().minusSeconds(3600));
            List<ProjectActivityDto> activities = List.of(activity1, activity2);
            Page<ProjectActivityDto> mockedPage = new PageImpl<>(activities);

            when(projectActivityService.findActivitiesByProjectId(eq(testUserId), eq(testProjectId), any(Pageable.class)))
                    .thenReturn(mockedPage);

            // When & Then
            mockMvc.perform(get(API_BASE_URL + "/projects/{projectId}/activities", testProjectId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            )))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].actorName").value("actor1"))
                    .andExpect(jsonPath("$.totalElements").value(2));

            verify(projectActivityService).findActivitiesByProjectId(eq(testUserId), eq(testProjectId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return 200 OK and an empty page when no activities exist")
        void getProjectActivities_whenNoActivities_shouldReturn200AndEmptyPage() throws Exception {
            // Given
            when(projectActivityService.findActivitiesByProjectId(eq(testUserId), eq(testProjectId), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // When & Then
            mockMvc.perform(get(API_BASE_URL + "/projects/{projectId}/activities", testProjectId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(projectActivityService).findActivitiesByProjectId(eq(testUserId), eq(testProjectId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should pass custom pagination and sorting parameters to service")
        void getProjectActivities_withCustomParams_shouldCallServiceWithCorrectPageable() throws Exception {
            // Given
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(projectActivityService.findActivitiesByProjectId(any(), any(), any())).thenReturn(Page.empty());

            // When
            mockMvc.perform(get(API_BASE_URL + "/projects/{projectId}/activities", testProjectId)
                            .param("page", "2")
                            .param("size", "5")
                            .param("sortBy", "actorName")
                            .param("sortDir", "asc")
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
                    .andExpect(status().isOk());

            // Then
            verify(projectActivityService).findActivitiesByProjectId(eq(testUserId), eq(testProjectId), pageableCaptor.capture());
            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(capturedPageable.getPageNumber()).isEqualTo(2);
            assertThat(capturedPageable.getPageSize()).isEqualTo(5);
            assertThat(capturedPageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "actorName"));
        }
    }
}
