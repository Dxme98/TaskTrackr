package com.dev.tasktrackr.ProjectTests.controller;


import com.dev.tasktrackr.config.JpaAuditingConfig;
import com.dev.tasktrackr.project.api.controller.ProjectInviteController;
import com.dev.tasktrackr.project.api.dtos.request.ProjectInviteRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import com.dev.tasktrackr.project.service.ProjectInviteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProjectInviteController.class)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
@DisplayName("ProjectInviteController (WebMvcTest)")
public class ProjectInviteControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectInviteService projectInviteService;

    private static final String API_BASE_URL = "/api/v1";
    private String testUserId;
    private String testUsername;
    private Long testProjectId;
    private Long testInviteId;

    @BeforeEach
    void setUp() {
        testUserId = "user-id-123";
        testUsername = "testuser";
        testProjectId = 1L;
        testInviteId = 1L;
    }

    @Test
    @DisplayName("POST /projects/{projectId}/invites - Sollte 201 Created zurückgeben und Einladung erstellen")
    void createInvite_whenValidRequest_shouldReturn201AndInvite() throws Exception {
        // Given
        ProjectInviteRequest request = new ProjectInviteRequest();
        request.setReceiverUsername("receiverUser");

        ProjectInviteResponseDto expectedResponse = new ProjectInviteResponseDto(
                1L,
                testUserId,
                testUsername,
                "receiver-id-456",
                "receiverUser",
                testProjectId,
                "Test Projekt",
                ProjectInviteStatus.PENDING,
                Instant.now(),
                Instant.now()
        );

        when(projectInviteService.createProjectInvite(any(ProjectInviteRequest.class), eq(testUserId), eq(testProjectId)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post(API_BASE_URL + "/projects/{projectId}/invites", testProjectId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
                .andExpect(jsonPath("$.senderId").value(expectedResponse.getSenderId()))
                .andExpect(jsonPath("$.receiverId").value(expectedResponse.getReceiverId()))
                .andExpect(jsonPath("$.projectId").value(expectedResponse.getProjectId()))
                .andExpect(jsonPath("$.inviteStatus").value("PENDING"));

        verify(projectInviteService).createProjectInvite(any(ProjectInviteRequest.class), eq(testUserId), eq(testProjectId));
    }

    @Test
    @DisplayName("PUT /invites/{inviteId}/accept - Sollte 200 OK zurückgeben und Einladung akzeptieren")
    void acceptInvite_whenValidRequest_shouldReturn200AndAcceptedInvite() throws Exception {
        // Given
        ProjectInviteResponseDto expectedResponse = new ProjectInviteResponseDto(
                testInviteId,
                "sender-id-789",
                "senderUser",
                testUserId,
                testUsername,
                testProjectId,
                "Test Projekt",
                ProjectInviteStatus.ACCEPTED,
                Instant.now(),
                Instant.now()
        );

        when(projectInviteService.acceptProjectInvite(eq(testUserId), eq(testInviteId)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(put(API_BASE_URL + "/invites/{inviteId}/accept", testInviteId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
                .andExpect(jsonPath("$.receiverId").value(expectedResponse.getReceiverId()))
                .andExpect(jsonPath("$.inviteStatus").value("ACCEPTED"));

        verify(projectInviteService).acceptProjectInvite(eq(testUserId), eq(testInviteId));
    }

    @Test
    @DisplayName("PUT /invites/{inviteId}/decline - Sollte 200 OK zurückgeben und Einladung ablehnen")
    void declineInvite_whenValidRequest_shouldReturn200AndDeclinedInvite() throws Exception {
        // Given
        ProjectInviteResponseDto expectedResponse = new ProjectInviteResponseDto(
                testInviteId,
                "sender-id-789",
                "senderUser",
                testUserId,
                testUsername,
                testProjectId,
                "Test Projekt",
                ProjectInviteStatus.DECLINED,
                Instant.now(),
                Instant.now()
        );

        // When & Then
        mockMvc.perform(put(API_BASE_URL + "/invites/{inviteId}/decline", testInviteId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
                .andExpect(jsonPath("$.receiverId").value(expectedResponse.getReceiverId()))
                .andExpect(jsonPath("$.inviteStatus").value("DECLINED"));

        verify(projectInviteService).declineProjectInvite(eq(testUserId), eq(testInviteId));
    }

    @Test
    @DisplayName("GET /invites - Sollte 200 OK zurückgeben und ausstehende Einladungen laden")
    void findAllPendingInvitesByUserId_whenValidRequest_shouldReturn200AndPendingInvites() throws Exception {
        // Given
        int page = 0;
        int size = 5;

        List<ProjectInviteResponseDto> inviteList = Arrays.asList(
                new ProjectInviteResponseDto(
                        1L,
                        "sender-id-1",
                        "sender1",
                        testUserId,
                        testUsername,
                        1L,
                        "Projekt 1",
                        ProjectInviteStatus.PENDING,
                        Instant.now(),
                        Instant.now()
                ),
                new ProjectInviteResponseDto(
                        2L,
                        "sender-id-2",
                        "sender2",
                        testUserId,
                        testUsername,
                        2L,
                        "Projekt 2",
                        ProjectInviteStatus.PENDING,
                        Instant.now(),
                        Instant.now()
                )
        );

        Page<ProjectInviteResponseDto> mockPage = new PageImpl<>(inviteList, PageRequest.of(page, size), inviteList.size());

        when(projectInviteService.findAllPendingInvitesByUserId(eq(testUserId), any(PageRequest.class)))
                .thenReturn(mockPage);

        // When & Then - Test mit Default-Werten
        mockMvc.perform(get(API_BASE_URL + "/invites")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].projectName").value("Projekt 1"))
                .andExpect(jsonPath("$.content[0].inviteStatus").value("PENDING"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].projectName").value("Projekt 2"))
                .andExpect(jsonPath("$.content[1].inviteStatus").value("PENDING"))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(projectInviteService).findAllPendingInvitesByUserId(eq(testUserId), any(PageRequest.class));
    }
}
