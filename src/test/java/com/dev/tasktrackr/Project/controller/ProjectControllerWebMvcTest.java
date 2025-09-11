package com.dev.tasktrackr.Project.controller;

import com.dev.tasktrackr.config.JpaAuditingConfig;
import com.dev.tasktrackr.project.api.ProjectController;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType; // KORRIGIERT: Import
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
// KORRIGIERT: Alle statischen Imports für MockMvc
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(controllers = ProjectController.class)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
@DisplayName("ProjectController (WebMvcTest)")
class ProjectControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    private static final String API_BASE_URL = "/api/v1/projects";
    private String testUserId;
    private String testUsername;

    @BeforeEach
    void setUp() {
        testUserId = "user-id-123";
        testUsername = "testuser";
    }

    @Test
    @DisplayName("POST /projects - Sollte 201 Created zurückgeben und das Projekt bei gültiger Anfrage erstellen")
    void createProject_whenValidRequest_shouldReturn201AndProject() throws Exception {
        // Given
        ProjectRequest request = new ProjectRequest("Neues Super-Projekt", ProjectType.SCRUM);
        ProjectOverviewDto expectedResponse = ProjectOverviewDto.builder()
                .id(1L)
                .name("Neues Super-Projekt")
                .projectType(ProjectType.SCRUM)
                .createdAt(Instant.now())
                .build();

        when(projectService.createProject(eq(testUserId), any(ProjectRequest.class)))
                .thenReturn(expectedResponse);

        // When & Then - Diese Sektion funktioniert jetzt dank der korrekten Imports
        mockMvc.perform(post(API_BASE_URL)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        ))
                        .contentType(MediaType.APPLICATION_JSON) // Funktioniert jetzt
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Funktioniert jetzt
                .andExpect(jsonPath("$.id").value(expectedResponse.getId())) // Funktioniert jetzt
                .andExpect(jsonPath("$.name").value(expectedResponse.getName()));

        verify(projectService).createProject(eq(testUserId), any(ProjectRequest.class));
    }
}