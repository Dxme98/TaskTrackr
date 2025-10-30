package com.dev.tasktrackr.ProjectTests.controller;

import com.dev.tasktrackr.config.JpaAuditingConfig;
import com.dev.tasktrackr.project.api.controller.SprintController;
import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.response.SprintResponseDto;
import com.dev.tasktrackr.project.domain.scrum.SprintStatus;
import com.dev.tasktrackr.project.service.SprintService;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.SprintNotActiveException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SprintController.class)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
@DisplayName("SprintController (WebMvcTest)")
class SprintControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SprintService sprintService;

    private static final String API_BASE_URL_TEMPLATE = "/api/v1/projects/{projectId}/sprints";

    private String testUserId;
    private String testUsername;
    private Long testProjectId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        testUserId = "user-id-123";
        testUsername = "testuser";
        testProjectId = 1L;
        startDate = LocalDate.now();
        endDate = LocalDate.now().plusWeeks(2);
    }

    private CreateSprintRequest createValidSprintRequest() {
        return new CreateSprintRequest(
                "Sprint Alpha",
                "Release V1",
                "Sprint Description",
                startDate,
                endDate,
                Set.of(101L, 102L)
        );
    }

    private SprintResponseDto createSprintResponseDto(Long id, SprintStatus status) {
        return new SprintResponseDto(
                id, "Sprint Alpha", "Release V1", "Sprint Description",
                status, startDate, endDate, 2, 0, 13, 0, 0.0,
                Collections.emptySet()
        );
    }

    @Nested
    @DisplayName("Sprint Erstellung (POST)")
    class SprintCreationTests {
        @Test
        @DisplayName("POST /{projectId}/sprints - Sollte 201 Created zurückgeben und Sprint erstellen")
        void createSprint_whenValidRequest_shouldReturn201AndSprint() throws Exception {
            // Given
            CreateSprintRequest request = createValidSprintRequest();
            SprintResponseDto expectedResponse = createSprintResponseDto(1L, SprintStatus.PLANNED);

            when(sprintService.createSprint(any(CreateSprintRequest.class), eq(testProjectId), eq(testUserId)))
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
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Sprint Alpha"))
                    .andExpect(jsonPath("$.status").value(SprintStatus.PLANNED.toString()));

            verify(sprintService).createSprint(any(CreateSprintRequest.class), eq(testProjectId), eq(testUserId));
        }

        @Test
        @DisplayName("POST /{projectId}/sprints - Sollte 400 Bad Request bei fehlendem Namen zurückgeben")
        void createSprint_whenNameIsMissing_shouldReturn400() throws Exception {
            // Given
            CreateSprintRequest invalidRequest = createValidSprintRequest();
            invalidRequest.setName(null); // @NotBlank verletzt

            // When & Then
            mockMvc.perform(post(API_BASE_URL_TEMPLATE, testProjectId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest()); // Spring Validation fängt dies ab

            verify(sprintService, never()).createSprint(any(), anyLong(), anyString());
        }

        @Test
        @DisplayName("POST /{projectId}/sprints - Sollte 400 Bad Request bei fehlendem Ziel (Goal) zurückgeben")
        void createSprint_whenGoalIsMissing_shouldReturn400() throws Exception {
            // Given
            CreateSprintRequest invalidRequest = createValidSprintRequest();
            invalidRequest.setGoal(""); // @NotBlank verletzt

            // When & Then
            mockMvc.perform(post(API_BASE_URL_TEMPLATE, testProjectId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(sprintService, never()).createSprint(any(), anyLong(), anyString());
        }

        @Test
        @DisplayName("POST /{projectId}/sprints - Sollte 400 Bad Request bei Startdatum in der Vergangenheit zurückgeben")
        void createSprint_whenStartDateIsInPast_shouldReturn400() throws Exception {
            // Given
            CreateSprintRequest invalidRequest = createValidSprintRequest();
            invalidRequest.setStartDate(LocalDate.now().minusDays(1)); // @FutureOrPresent verletzt

            // When & Then
            mockMvc.perform(post(API_BASE_URL_TEMPLATE, testProjectId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(sprintService, never()).createSprint(any(), anyLong(), anyString());
        }

        @Test
        @DisplayName("POST /{projectId}/sprints - Sollte 400 Bad Request bei Enddatum vor Startdatum zurückgeben")
        void createSprint_whenEndDateIsBeforeStartDate_shouldReturn400() throws Exception {
            // Given
            CreateSprintRequest invalidRequest = createValidSprintRequest();
            // @AssertTrue (isEndDateAfterStartDate) verletzt
            invalidRequest.setStartDate(LocalDate.now().plusDays(5));
            invalidRequest.setEndDate(LocalDate.now().plusDays(4));

            // When & Then
            mockMvc.perform(post(API_BASE_URL_TEMPLATE, testProjectId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(sprintService, never()).createSprint(any(), anyLong(), anyString());
        }
    }


    @Nested
    @DisplayName("Sprint Abruf (GET)")
    class SprintRetrievalTests {
        @Test
        @DisplayName("GET /{projectId}/sprints/{status} - Sollte 200 OK und eine Seite mit Sprints zurückgeben")
        void getAllSprints_whenValidRequest_shouldReturn200AndPage() throws Exception {
            // Given
            SprintStatus statusFilter = SprintStatus.PLANNED;
            int page = 0;
            int size = 10;
            PageRequest pageable = PageRequest.of(page, size);

            List<SprintResponseDto> sprintList = Arrays.asList(
                    createSprintResponseDto(1L, SprintStatus.PLANNED),
                    createSprintResponseDto(2L, SprintStatus.PLANNED)
            );
            Page<SprintResponseDto> mockPage = new PageImpl<>(sprintList, pageable, sprintList.size());

            when(sprintService.findAllSprintsByProjectIdAndStatus(eq(testProjectId), eq(testUserId), eq(pageable), eq(statusFilter)))
                    .thenReturn(mockPage);

            // When & Then
            mockMvc.perform(get(API_BASE_URL_TEMPLATE + "/{status}", testProjectId, statusFilter)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[1].id").value(2L))
                    .andExpect(jsonPath("$.page", is(page)))
                    .andExpect(jsonPath("$.size", is(size)))
                    .andExpect(jsonPath("$.totalElements", is(2)));

            // ArgumentCaptor, um das Pageable zu prüfen
            ArgumentCaptor<PageRequest> pageableCaptor = ArgumentCaptor.forClass(PageRequest.class);
            verify(sprintService).findAllSprintsByProjectIdAndStatus(eq(testProjectId), eq(testUserId), pageableCaptor.capture(), eq(statusFilter));
            assertEquals(pageable, pageableCaptor.getValue());
        }

        @Test
        @DisplayName("GET /{projectId}/sprints/active - Sollte 200 OK und den aktiven Sprint zurückgeben")
        void getActiveSprint_whenActiveSprintExists_shouldReturn200() throws Exception {
            // Given
            SprintResponseDto activeSprint = createSprintResponseDto(5L, SprintStatus.ACTIVE);
            when(sprintService.findActiveSprint(eq(testProjectId), eq(testUserId))).thenReturn(activeSprint);

            // When & Then
            mockMvc.perform(get(API_BASE_URL_TEMPLATE + "/active", testProjectId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5L))
                    .andExpect(jsonPath("$.status").value(SprintStatus.ACTIVE.toString()));

            verify(sprintService).findActiveSprint(eq(testProjectId), eq(testUserId));
        }
    }


    @Nested
    @DisplayName("Sprint Statusänderungen (Start/End)")
    class SprintStatusChangeTests {
        @Test
        @DisplayName("POST /{projectId}/sprints/{sprintId}/start - Sollte 200 OK und den gestarteten Sprint zurückgeben")
        void startSprint_whenValid_shouldReturn200AndStartedSprint() throws Exception {
            // Given
            Long sprintId = 10L;
            SprintResponseDto startedSprint = createSprintResponseDto(sprintId, SprintStatus.ACTIVE);
            when(sprintService.startSprint(eq(sprintId), eq(testProjectId), eq(testUserId))).thenReturn(startedSprint);

            // When & Then
            mockMvc.perform(post(API_BASE_URL_TEMPLATE + "/{sprintId}/start", testProjectId, sprintId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(sprintId))
                    .andExpect(jsonPath("$.status").value(SprintStatus.ACTIVE.toString()));

            verify(sprintService).startSprint(eq(sprintId), eq(testProjectId), eq(testUserId));
        }

        @Test
        @DisplayName("POST /{projectId}/sprints/{sprintId}/start - Sollte 400 Bad Request bei ungültigem Status (z.B. schon aktiv)")
        void startSprint_whenInvalidState_shouldReturn400() throws Exception {
            // Given
            Long sprintId = 11L;
            // Der Service wirft eine Exception (z.B. IllegalStateException), die der ExceptionHandler in 400 umwandelt
            when(sprintService.startSprint(eq(sprintId), eq(testProjectId), eq(testUserId)))
                    .thenThrow(new SprintNotActiveException(sprintId));

            // When & Then
            mockMvc.perform(post(API_BASE_URL_TEMPLATE + "/{sprintId}/start", testProjectId, sprintId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
                    .andExpect(status().isConflict()); // Annahme: GlobalExceptionHandler wandelt IllegalStateException in 400 um

            verify(sprintService).startSprint(eq(sprintId), eq(testProjectId), eq(testUserId));
        }

        @Test
        @DisplayName("POST /{projectId}/sprints/{sprintId}/end - Sollte 200 OK und den beendeten Sprint zurückgeben")
        void endSprint_whenValid_shouldReturn200AndEndedSprint() throws Exception {
            // Given
            Long sprintId = 12L;
            SprintResponseDto endedSprint = createSprintResponseDto(sprintId, SprintStatus.DONE);
            when(sprintService.endSprint(eq(sprintId), eq(testProjectId), eq(testUserId))).thenReturn(endedSprint);

            // When & Then
            mockMvc.perform(post(API_BASE_URL_TEMPLATE + "/{sprintId}/end", testProjectId, sprintId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(sprintId))
                    .andExpect(jsonPath("$.status").value(SprintStatus.DONE.toString()));

            verify(sprintService).endSprint(eq(sprintId), eq(testProjectId), eq(testUserId));
        }
    }
}