package com.dev.tasktrackr.Project.controller;

import com.dev.tasktrackr.config.JpaAuditingConfig;
import com.dev.tasktrackr.project.api.controller.ProjectMemberController;
import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.service.ProjectMemberService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ProjectMemberController.class)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
@DisplayName("ProjectMemberController (WebMvcTest)")
class ProjectMemberControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectMemberService projectMemberService;

    private static final String API_BASE_URL = "/api/v1/projects";
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
    @DisplayName("GET /projects/{projectId}/members - Sollte 200 OK zurückgeben und Projektmitglieder laden")
    void findAllProjectMembersByProjectId_whenValidRequest_shouldReturn200AndMembers() throws Exception {
        // Given
        int page = 0;
        int size = 5;

        List<ProjectMemberDto> memberList = Arrays.asList(
                ProjectMemberDto.builder()
                        .id(1L)
                        .userId("user-1")
                        .username("member1")
                        .projectId("1")
                        .role("OWNER")
                        .permissions(Set.of(PermissionName.BASIC_CREATE_TASK, PermissionName.COMMON_INVITE_USER))
                        .build(),
                ProjectMemberDto.builder()
                        .id(2L)
                        .userId("user-2")
                        .username("member2")
                        .projectId("1")
                        .role("MEMBER")
                        .permissions(Set.of(PermissionName.COMMON_INVITE_USER))
                        .build()
        );

        Page<ProjectMemberDto> mockPage = new PageImpl<>(memberList, PageRequest.of(page, size), memberList.size());

        when(projectMemberService.getAllProjectMembers(eq(testUserId), eq(testProjectId), any(PageRequest.class)))
                .thenReturn(mockPage);

        // When & Then - Test mit Default-Werten
        mockMvc.perform(get(API_BASE_URL + "/{projectId}/members", testProjectId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].username").value("member1"))
                .andExpect(jsonPath("$.content[0].role").value("OWNER"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].username").value("member2"))
                .andExpect(jsonPath("$.content[1].role").value("MEMBER"))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

        verify(projectMemberService).getAllProjectMembers(eq(testUserId), eq(testProjectId), any(PageRequest.class));
    }

    @Test
    @DisplayName("DELETE /projects/{projectId}/members/{memberId} - Sollte 204 No Content zurückgeben und Mitglied entfernen")
    void removeMember_whenValidRequest_shouldReturn204() throws Exception {
        // Given
        Long memberId = 2L;

        // When & Then
        mockMvc.perform(delete(API_BASE_URL + "/{projectId}/members/{memberId}", testProjectId, memberId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isNoContent());

        verify(projectMemberService).removeMemberFromProject(eq(testUserId), eq(testProjectId), eq(memberId));
    }
}