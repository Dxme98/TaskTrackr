package com.dev.tasktrackr.ProjectTests.controller;

import com.dev.tasktrackr.config.JpaAuditingConfig;
import com.dev.tasktrackr.project.api.controller.ScrumBoardController;
import com.dev.tasktrackr.project.api.dtos.request.AssignMemberRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateStatusRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumBoardResponseDto;
import com.dev.tasktrackr.project.api.dtos.response.SprintBacklogItemResponse;
import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import com.dev.tasktrackr.project.service.ScrumBoardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ScrumBoardController.class)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
@DisplayName("ScrumBoardController (WebMvcTest)")
class ScrumBoardControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScrumBoardService scrumBoardService;

    private static final String API_BASE_URL_TEMPLATE = "/api/v1/projects/{projectId}/scrumboard";

    private String testUserId;
    private String testUsername;
    private Long testProjectId;
    private SprintBacklogItemResponse mockItemResponse;

    @BeforeEach
    void setUp() {
        testUserId = "user-id-123";
        testUsername = "testuser";
        testProjectId = 1L;

        // Ein generisches Mock-Response-Objekt für Operationen, die ein Item zurückgeben
        mockItemResponse = new SprintBacklogItemResponse(
                42L, "Test Item", "Description", 5, Priority.MEDIUM,
                StoryStatus.IN_PROGRESS, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
    }

    private ScrumBoardResponseDto createMockScrumBoardResponseDto() {
        ScrumBoardResponseDto dto = new ScrumBoardResponseDto();
        dto.setSprintName("Active Sprint");
        dto.setSprintGoal("Test Goal");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusWeeks(2));
        dto.setTotalStoryPoints(100);
        dto.setCompletedStoryPoints(20);
        dto.setProjectMembers(List.of(ProjectMemberDto.builder().id(1L).username("member1").build()));
        dto.setTodo(List.of(
                new SprintBacklogItemResponse(1L, "Todo Task", "Desc", 3, Priority.LOW, StoryStatus.SPRINT_BACKLOG, new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
        ));
        dto.setInProgress(List.of(
                new SprintBacklogItemResponse(2L, "In Progress Task", "Desc", 5, Priority.HIGH, StoryStatus.IN_PROGRESS, new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
        ));
        return dto;
    }


    @Nested
    @DisplayName("GET /scrumboard")
    class GetScrumBoardTests {

        @Test
        @DisplayName("Sollte 200 OK und das Scrum Board DTO zurückgeben")
        void getActiveScrumBoard_whenFound_shouldReturn200AndBoard() throws Exception {
            // Given
            ScrumBoardResponseDto mockBoard = createMockScrumBoardResponseDto();
            when(scrumBoardService.getScrumBoard(eq(testProjectId), eq(testUserId))).thenReturn(mockBoard);

            // When & Then
            mockMvc.perform(get(API_BASE_URL_TEMPLATE, testProjectId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            )))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.sprintName", is("Active Sprint")))
                    .andExpect(jsonPath("$.sprintGoal", is("Test Goal")))
                    .andExpect(jsonPath("$.totalStoryPoints", is(100)))
                    .andExpect(jsonPath("$.projectMembers", hasSize(1)))
                    .andExpect(jsonPath("$.todo", hasSize(1)))
                    .andExpect(jsonPath("$.inProgress", hasSize(1)))
                    .andExpect(jsonPath("$.review", hasSize(0)))
                    .andExpect(jsonPath("$.done", hasSize(0)));

            verify(scrumBoardService).getScrumBoard(eq(testProjectId), eq(testUserId));
        }
    }

    @Nested
    @DisplayName("PATCH /items/{itemId}/status")
    class UpdateStatusTests {

        @Test
        @DisplayName("Sollte 200 OK und das aktualisierte Item bei Statusänderung zurückgeben")
        void updateUserStoryStatus_whenValid_shouldReturn200() throws Exception {
            // Given
            Long backlogItemId = 42L;
            StoryStatus newStatus = StoryStatus.REVIEW;
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setNewStatus(newStatus);

            mockItemResponse.setStatus(newStatus); // Antwort anpassen
            when(scrumBoardService.updateUserStoryStatus(eq(testProjectId), eq(backlogItemId), eq(newStatus), eq(testUserId)))
                    .thenReturn(mockItemResponse);

            // When & Then
            mockMvc.perform(patch(API_BASE_URL_TEMPLATE + "/items/{backlogItemId}/status", testProjectId, backlogItemId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(42)))
                    .andExpect(jsonPath("$.status", is(newStatus.toString())));

            verify(scrumBoardService).updateUserStoryStatus(eq(testProjectId), eq(backlogItemId), eq(newStatus), eq(testUserId));
        }

        @Test
        @DisplayName("Sollte 400 Bad Request zurückgeben, wenn der Status im Request fehlt")
        void updateUserStoryStatus_whenInvalidRequest_shouldReturn400() throws Exception {
            // Given
            Long backlogItemId = 42L;
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setNewStatus(null); // @NotNull verletzt

            // When & Then
            mockMvc.perform(patch(API_BASE_URL_TEMPLATE + "/items/{backlogItemId}/status", testProjectId, backlogItemId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(scrumBoardService, never()).updateUserStoryStatus(anyLong(), anyLong(), any(), anyString());
        }
    }

    @Nested
    @DisplayName("POST & DELETE /items/{itemId}/assignees")
    class AssigneeTests {

        @Test
        @DisplayName("POST .../assignees - Sollte 200 OK bei erfolgreicher Zuweisung zurückgeben")
        void assignMember_whenValid_shouldReturn200() throws Exception {
            // Given
            Long backlogItemId = 42L;
            Long memberId = 5L;
            AssignMemberRequest request = new AssignMemberRequest(memberId);

            when(scrumBoardService.assignMemberToStory(eq(testProjectId), eq(backlogItemId), eq(memberId), eq(testUserId)))
                    .thenReturn(mockItemResponse);

            // When & Then
            mockMvc.perform(post(API_BASE_URL_TEMPLATE + "/items/{backlogItemId}/assignees", testProjectId, backlogItemId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(42)));

            verify(scrumBoardService).assignMemberToStory(eq(testProjectId), eq(backlogItemId), eq(memberId), eq(testUserId));
        }

        @Test
        @DisplayName("DELETE .../assignees/{memberId} - Sollte 200 OK bei erfolgreicher Zuweisungsentfernung zurückgeben")
        void unassignMember_whenValid_shouldReturn200() throws Exception {
            // Given
            Long backlogItemId = 42L;
            Long memberId = 5L;

            when(scrumBoardService.unassignMemberFromStory(eq(testProjectId), eq(backlogItemId), eq(memberId), eq(testUserId)))
                    .thenReturn(mockItemResponse);

            // When & Then
            mockMvc.perform(delete(API_BASE_URL_TEMPLATE + "/items/{backlogItemId}/assignees/{memberId}", testProjectId, backlogItemId, memberId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(42)));

            verify(scrumBoardService).unassignMemberFromStory(eq(testProjectId), eq(backlogItemId), eq(memberId), eq(testUserId));
        }
    }

    @Nested
    @DisplayName("POST & DELETE /items/{itemId}/comments")
    class CommentTests {

        @Test
        @DisplayName("POST .../comments - Sollte 201 Created bei Hinzufügen eines Kommentars zurückgeben")
        void addComment_whenValid_shouldReturn201() throws Exception {
            // Given
            Long backlogItemId = 42L;
            CreateCommentRequest request = new CreateCommentRequest("Test kommentar");

            when(scrumBoardService.addCommentToStory(eq(testProjectId), eq(backlogItemId), any(CreateCommentRequest.class), eq(testUserId)))
                    .thenReturn(mockItemResponse);

            // When & Then
            mockMvc.perform(post(API_BASE_URL_TEMPLATE + "/items/{backlogItemId}/comments", testProjectId, backlogItemId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(42)));

            verify(scrumBoardService).addCommentToStory(eq(testProjectId), eq(backlogItemId), any(CreateCommentRequest.class), eq(testUserId));
        }

        @Test
        @DisplayName("DELETE .../comments/{commentId} - Sollte 204 No Content bei Löschen eines Kommentars zurückgeben")
        void removeComment_whenValid_shouldReturn204() throws Exception {
            // Given
            Long backlogItemId = 42L;
            Long commentId = 101L;
            doNothing().when(scrumBoardService).removeCommentFromStory(eq(testProjectId), eq(backlogItemId), eq(commentId), eq(testUserId));

            // When & Then
            mockMvc.perform(delete(API_BASE_URL_TEMPLATE + "/items/{backlogItemId}/comments/{commentId}", testProjectId, backlogItemId, commentId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
                    .andExpect(status().isNoContent());

            verify(scrumBoardService).removeCommentFromStory(eq(testProjectId), eq(backlogItemId), eq(commentId), eq(testUserId));
        }
    }

    @Nested
    @DisplayName("POST & DELETE /items/{itemId}/blockers")
    class BlockerTests {

        @Test
        @DisplayName("POST .../blockers - Sollte 201 Created bei Hinzufügen eines Blockers zurückgeben")
        void addBlocker_whenValid_shouldReturn201() throws Exception {
            // Given
            Long backlogItemId = 42L;
            CreateCommentRequest request = new CreateCommentRequest("Test blocker");

            when(scrumBoardService.addBlockerToStory(eq(testProjectId), eq(backlogItemId), any(CreateCommentRequest.class), eq(testUserId)))
                    .thenReturn(mockItemResponse);

            // When & Then
            mockMvc.perform(post(API_BASE_URL_TEMPLATE + "/items/{backlogItemId}/blockers", testProjectId, backlogItemId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(42)));

            verify(scrumBoardService).addBlockerToStory(eq(testProjectId), eq(backlogItemId), any(CreateCommentRequest.class), eq(testUserId));
        }

        @Test
        @DisplayName("DELETE .../blockers/{blockerId} - Sollte 204 No Content bei Löschen eines Blockers zurückgeben")
        void removeBlocker_whenValid_shouldReturn204() throws Exception {
            // Given
            Long backlogItemId = 42L;
            Long blockerId = 102L;
            doNothing().when(scrumBoardService).removeBlockerFromStory(eq(testProjectId), eq(backlogItemId), eq(blockerId), eq(testUserId));

            // When & Then
            mockMvc.perform(delete(API_BASE_URL_TEMPLATE + "/items/{backlogItemId}/blockers/{blockerId}", testProjectId, backlogItemId, blockerId)
                            .with(jwt().jwt(jwt -> jwt.claim("sub", testUserId))))
                    .andExpect(status().isNoContent());

            verify(scrumBoardService).removeBlockerFromStory(eq(testProjectId), eq(backlogItemId), eq(blockerId), eq(testUserId));
        }
    }
}