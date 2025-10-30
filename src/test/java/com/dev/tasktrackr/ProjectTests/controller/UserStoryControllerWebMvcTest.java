package com.dev.tasktrackr.ProjectTests.controller;

import com.dev.tasktrackr.config.JpaAuditingConfig;
import com.dev.tasktrackr.project.api.controller.UserStoryController;
import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import com.dev.tasktrackr.project.service.UserStoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserStoryController.class)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
@DisplayName("UserStoryController (WebMvcTest)")
class UserStoryControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserStoryService userStoryService;

    private static final String API_BASE_URL_TEMPLATE = "/api/v1/projects/{projectId}/userStories";

    private String testUserId;
    private String testUsername;
    private Long testProjectId;

    @BeforeEach
    void setUp() {
        testUserId = "user-id-123";
        testUsername = "testuser";
        testProjectId = 1L;
    }

    @Test
    @DisplayName("POST /{projectId}/userStories - Sollte 201 Created zurückgeben und User Story erstellen")
    void createUserStory_whenValidRequest_shouldReturn201AndUserStory() throws Exception {
        // Given
        CreateUserStoryRequest request = new CreateUserStoryRequest(
                "Beispiel UserStoryTitle",
                "Beispiel UserStoryBeispiel",
                Priority.HIGH,
                5
        );

        Instant fixedTime = Instant.parse("2025-10-10T10:00:00Z");
        UserStoryResponseDto expectedResponse = new UserStoryResponseDto(
                101L,
                "Beispiel UserStoryTitle",
                null, // sprintName ist null bei Erstellung
                "Beispiel UserStoryBeispiel",
                Priority.HIGH,
                5,
                fixedTime,
                StoryStatus.NOT_ASSIGNED_TO_SPRINT
        );

        when(userStoryService.createUserStory(eq(testProjectId), any(CreateUserStoryRequest.class), eq(testUserId)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post(API_BASE_URL_TEMPLATE, testProjectId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(101L))
                .andExpect(jsonPath("$.title").value("Beispiel UserStoryTitle"))
                .andExpect(jsonPath("$.status").value(StoryStatus.NOT_ASSIGNED_TO_SPRINT.toString()))
                .andExpect(jsonPath("$.storyPoints").value(5))
                .andExpect(jsonPath("$.createdAt").value(fixedTime.toString()));

        verify(userStoryService).createUserStory(eq(testProjectId), any(CreateUserStoryRequest.class), eq(testUserId));
    }

    @Test
    @DisplayName("POST /{projectId}/userStories - Sollte 400 Bad Request bei ungültiger Anfrage zurückgeben (z.B. Titel fehlt)")
    void createUserStory_whenInvalidRequest_shouldReturn400() throws Exception {
        // Given
        // Titel fehlt -> @NotBlank verletzt
        CreateUserStoryRequest invalidRequest = new CreateUserStoryRequest(
                null, "Beschreibung", Priority.LOW, 3
        );

        // When & Then
        mockMvc.perform(post(API_BASE_URL_TEMPLATE, testProjectId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Service darf nicht aufgerufen werden
        verify(userStoryService, never()).createUserStory(anyLong(), any(), anyString());
    }

    @Test
    @DisplayName("DELETE /{projectId}/userStories/{userStoryId} - Sollte 204 No Content bei erfolgreichem Löschen zurückgeben")
    void deleteUserStory_whenValidRequest_shouldReturn204() throws Exception {
        // Given
        Long userStoryId = 55L;
        // Service-Methode ist void
        doNothing().when(userStoryService).deleteUserStory(eq(testProjectId), eq(userStoryId), eq(testUserId));

        // When & Then
        mockMvc.perform(delete(API_BASE_URL_TEMPLATE + "/{userStoryId}", testProjectId, userStoryId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isNoContent());

        verify(userStoryService).deleteUserStory(eq(testProjectId), eq(userStoryId), eq(testUserId));
    }

    @Test
    @DisplayName("GET /{projectId}/userStories - Sollte 200 OK und eine Seite mit User Stories zurückgeben (Standard)")
    void getUserStories_whenDefaultParams_shouldReturn200AndPage() throws Exception {
        // Given
        int page = 0;
        int size = 20;
        String sortBy = "createdAt";
        String sortDir = "asc";

        UserStoryResponseDto story1 = new UserStoryResponseDto(1L, "Story 1", null, "Desc 1", Priority.LOW, 3, Instant.now(), StoryStatus.NOT_ASSIGNED_TO_SPRINT);
        UserStoryResponseDto story2 = new UserStoryResponseDto(2L, "Story 2", "Sprint Alpha", "Desc 2", Priority.HIGH, 8, Instant.now(), StoryStatus.IN_PROGRESS);
        List<UserStoryResponseDto> storyList = Arrays.asList(story1, story2);

        Pageable expectedPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy));
        Page<UserStoryResponseDto> mockPage = new PageImpl<>(storyList, expectedPageable, storyList.size());

        // Standardaufruf: filter=null
        when(userStoryService.getUserStoriesByProjectId(eq(testProjectId), any(Pageable.class), eq(testUserId), eq(null)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get(API_BASE_URL_TEMPLATE, testProjectId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sortBy", sortBy)
                        .param("sortDir", sortDir))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title", is("Story 1")))
                .andExpect(jsonPath("$.content[1].title", is("Story 2")))
                .andExpect(jsonPath("$.content[1].sprintName", is("Sprint Alpha")))
                .andExpect(jsonPath("$.page", is(page)))
                .andExpect(jsonPath("$.size", is(size)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.last", is(true)));

        verify(userStoryService).getUserStoriesByProjectId(eq(testProjectId), eq(expectedPageable), eq(testUserId), eq(null));
    }

    @Test
    @DisplayName("GET /{projectId}/userStories - Sollte 200 OK und Service mit Filter/Sortierparametern aufrufen")
    void getUserStories_whenFilteredAndSorted_shouldCallServiceWithFilters() throws Exception {
        // Given
        int page = 1;
        int size = 5;
        String sortBy = "priority";
        String sortDir = "desc";
        String filter = "BACKLOG"; // Angenommener Filterwert

        Page<UserStoryResponseDto> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);

        when(userStoryService.getUserStoriesByProjectId(eq(testProjectId), any(Pageable.class), eq(testUserId), eq(filter)))
                .thenReturn(emptyPage);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        // When & Then
        mockMvc.perform(get(API_BASE_URL_TEMPLATE, testProjectId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sortBy", sortBy)
                        .param("sortDir", sortDir)
                        .param("filter", filter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page", is(page)));

        // Verifizieren, dass der Service mit den korrekten Filtern UND dem korrekten Pageable aufgerufen wurde
        verify(userStoryService).getUserStoriesByProjectId(eq(testProjectId), pageableCaptor.capture(), eq(testUserId), eq(filter));

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(page, capturedPageable.getPageNumber());
        assertEquals(size, capturedPageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "priority"), capturedPageable.getSort());
    }
}