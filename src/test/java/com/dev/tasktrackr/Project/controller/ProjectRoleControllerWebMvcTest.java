package com.dev.tasktrackr.Project.controller;

import com.dev.tasktrackr.config.JpaAuditingConfig;
import com.dev.tasktrackr.project.api.controller.ProjectRoleController;
import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.request.CreateProjectRoleRequest;
import com.dev.tasktrackr.project.api.dtos.request.RenameRoleRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.service.ProjectRoleService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;


import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProjectRoleController.class)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
@DisplayName("ProjectRoleController (WebMvcTest)")
class ProjectRoleControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectRoleService projectRoleService;

    private static final String API_BASE_URL = "/api/v1/projects";
    private String testUserId;
    private String testUsername;
    private Long testProjectId;
    private Integer testRoleId;
    private Long testMemberId;

    @BeforeEach
    void setUp() {
        testUserId = "user-id-123";
        testUsername = "testuser";
        testProjectId = 1L;
        testRoleId = 1;
        testMemberId = 1L;
    }

    @Test
    @DisplayName("POST /projects/{projectId}/roles - Sollte 201 Created zurückgeben und Rolle erstellen")
    void createRole_whenValidRequest_shouldReturn201AndRole() throws Exception {
        // Given
        CreateProjectRoleRequest request = new CreateProjectRoleRequest();
        request.setName("Test Rolle");
        request.setPermissions(Set.of(PermissionName.BASIC_CREATE_TASK, PermissionName.COMMON_INVITE_USER));

        ProjectRoleResponse expectedResponse = new ProjectRoleResponse(
                testRoleId,
                "Test Rolle",
                testProjectId,
                Set.of(PermissionName.BASIC_CREATE_TASK, PermissionName.COMMON_INVITE_USER),
                RoleType.CUSTOM
        );

        when(projectRoleService.createProjectRole(eq(testUserId), any(CreateProjectRoleRequest.class), eq(testProjectId)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post(API_BASE_URL + "/{projectId}/roles", testProjectId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
                .andExpect(jsonPath("$.name").value(expectedResponse.getName()))
                .andExpect(jsonPath("$.projectId").value(expectedResponse.getProjectId()))
                .andExpect(jsonPath("$.roleType").value("CUSTOM"));

        verify(projectRoleService).createProjectRole(eq(testUserId), any(CreateProjectRoleRequest.class), eq(testProjectId));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/roles - Sollte 200 OK zurückgeben und Rollen laden")
    void getAllRoles_whenValidRequest_shouldReturn200AndRoles() throws Exception {
        // Given
        int page = 0;
        int size = 10;

        List<ProjectRoleResponse> roleList = Arrays.asList(
                new ProjectRoleResponse(
                        1,
                        "Admin Rolle",
                        testProjectId,
                        Set.of(PermissionName.COMMON_MANAGE_ROLES, PermissionName.COMMON_INVITE_USER),
                        RoleType.CUSTOM
                ),
                new ProjectRoleResponse(
                        2,
                        "Member Rolle",
                        testProjectId,
                        Set.of(PermissionName.BASIC_CREATE_TASK),
                        RoleType.BASE
                )
        );

        Page<ProjectRoleResponse> mockPage = new PageImpl<>(roleList, PageRequest.of(page, size), roleList.size());

        when(projectRoleService.getAllRoles(eq(testUserId), any(PageRequest.class), eq(testProjectId)))
                .thenReturn(mockPage);

        // When & Then - Test mit Default-Werten
        mockMvc.perform(get(API_BASE_URL + "/{projectId}/roles", testProjectId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Admin Rolle"))
                .andExpect(jsonPath("$.content[0].roleType").value("CUSTOM"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Member Rolle"))
                .andExpect(jsonPath("$.content[1].roleType").value("BASE"))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(projectRoleService).getAllRoles(eq(testUserId), any(PageRequest.class), eq(testProjectId));
    }

    @Test
    @DisplayName("DELETE /projects/{projectId}/roles/{roleId} - Sollte 204 No Content zurückgeben und Rolle löschen")
    void deleteRole_whenValidRequest_shouldReturn204() throws Exception {
        // When & Then
        mockMvc.perform(delete(API_BASE_URL + "/{projectId}/roles/{roleId}", testProjectId, testRoleId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isNoContent());

        verify(projectRoleService).deleteProjectRole(eq(testUserId), eq(testProjectId), eq(testRoleId));
    }

    @Test
    @DisplayName("POST /projects/{projectId}/roles/{roleId}/assign/{memberId} - Sollte 200 OK zurückgeben und Rolle zuweisen")
    void assignRole_whenValidRequest_shouldReturn200AndAssignedMember() throws Exception {
        // Given
        ProjectMemberDto expectedResponse = ProjectMemberDto.builder()
                .id(testMemberId)
                .userId("member-user-id")
                .username("memberUser")
                .projectId(testProjectId.toString())
                .role("Test Rolle")
                .permissions(Set.of(PermissionName.BASIC_CREATE_TASK, PermissionName.COMMON_INVITE_USER))
                .build();

        when(projectRoleService.assignRole(eq(testUserId), eq(testRoleId), eq(testMemberId), eq(testProjectId)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post(API_BASE_URL + "/{projectId}/roles/{roleId}/assign/{memberId}",
                        testProjectId, testRoleId, testMemberId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
                .andExpect(jsonPath("$.username").value(expectedResponse.getUsername()))
                .andExpect(jsonPath("$.role").value(expectedResponse.getRole()));

        verify(projectRoleService).assignRole(eq(testUserId), eq(testRoleId), eq(testMemberId), eq(testProjectId));
    }

    @Test
    @DisplayName("PUT /projects/{projectId}/roles/{roleId}/rename - Sollte 200 OK zurückgeben und Rolle umbenennen")
    void renameRole_whenValidRequest_shouldReturn200AndRenamedRole() throws Exception {
        // Given
        RenameRoleRequest request = new RenameRoleRequest();
        request.setName("Umbenannte Rolle");

        ProjectRoleResponse expectedResponse = new ProjectRoleResponse(
                testRoleId,
                "Umbenannte Rolle",
                testProjectId,
                Set.of(PermissionName.BASIC_CREATE_TASK, PermissionName.COMMON_INVITE_USER),
                RoleType.CUSTOM
        );

        when(projectRoleService.renameRole(eq(testUserId), eq("Umbenannte Rolle"), eq(testProjectId), eq(testRoleId)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(put(API_BASE_URL + "/{projectId}/roles/{roleId}/rename", testProjectId, testRoleId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
                .andExpect(jsonPath("$.name").value(expectedResponse.getName()))
                .andExpect(jsonPath("$.projectId").value(expectedResponse.getProjectId()))
                .andExpect(jsonPath("$.roleType").value("CUSTOM"));

        verify(projectRoleService).renameRole(eq(testUserId), eq("Umbenannte Rolle"), eq(testProjectId), eq(testRoleId));
    }
}
