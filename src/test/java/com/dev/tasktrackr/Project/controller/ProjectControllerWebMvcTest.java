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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        // When & Then
        mockMvc.perform(post(API_BASE_URL)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
                .andExpect(jsonPath("$.name").value(expectedResponse.getName()));

        verify(projectService).createProject(eq(testUserId), any(ProjectRequest.class));
    }

    @Test
    @DisplayName("GET /projects - Sollte 200 OK zurückgeben und Projekte des Benutzers laden")
    void findAllProjectsByUserId_whenValidRequest_shouldReturn200AndProjects() throws Exception {
        // Given
        int page = 0;
        int size = 20;

        List<ProjectOverviewDto> projectList = Arrays.asList(
                ProjectOverviewDto.builder()
                        .id(1L)
                        .name("Projekt 1")
                        .projectType(ProjectType.SCRUM)
                        .createdAt(Instant.now())
                        .build(),
                ProjectOverviewDto.builder()
                        .id(2L)
                        .name("Projekt 2")
                        .projectType(ProjectType.BASIC)
                        .createdAt(Instant.now())
                        .build()
        );

        Page<ProjectOverviewDto> mockPage = new PageImpl<>(projectList, PageRequest.of(page, size), projectList.size());

        when(projectService.findProjectsByUserId(eq(testUserId), any(PageRequest.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get(API_BASE_URL)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        ))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Projekt 1"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Projekt 2"))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(projectService).findProjectsByUserId(eq(testUserId), any(PageRequest.class));
    }
}