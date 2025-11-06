package com.dev.tasktrackr.ProjectTests.controller;

import com.dev.tasktrackr.config.JpaAuditingConfig;
import com.dev.tasktrackr.basicdetails.api.controller.ProjectInformationController;
import com.dev.tasktrackr.basicdetails.api.dtos.request.CreateLinkRequest;
import com.dev.tasktrackr.basicdetails.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.basicdetails.domain.Information;
import com.dev.tasktrackr.basicdetails.domain.Link;
import com.dev.tasktrackr.basicdetails.domain.LinkType;
import com.dev.tasktrackr.basicdetails.service.ProjectInformationService;
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

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProjectInformationController.class)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
@DisplayName("ProjectInformationController (WebMvcTest)")
public class ProjectInformationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectInformationService projectInformationService;

    private static final String API_BASE_URL = "/api/v1";
    private String testUserId;
    private String testUsername;
    private Long testProjectId;
    private Long testLinkId;

    @BeforeEach
    void setUp() {
        testUserId = "user-id-123";
        testUsername = "testuser";
        testProjectId = 1L;
        testLinkId = 1L;
    }

    @Nested
    @DisplayName("GET /projects/{projectId}/information")
    class GetInformationTests {

        @Test
        @DisplayName("Should return 200 OK and information content")
        void findContentByProjectId_whenValidRequest_shouldReturn200AndInformation() throws Exception {
            // Given
            Information expectedInformation = new Information();
            expectedInformation.updateContent("This is the project information content");

            when(projectInformationService.findContentByProjectId(eq(testProjectId), eq(testUserId)))
                    .thenReturn(expectedInformation);

            // When & Then
            mockMvc.perform(get(API_BASE_URL + "/projects/{projectId}/information", testProjectId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            )))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value("This is the project information content"));

            verify(projectInformationService).findContentByProjectId(eq(testProjectId), eq(testUserId));
        }

        @Test
        @DisplayName("Should return 200 OK and empty content when no information exists")
        void findContentByProjectId_whenEmptyContent_shouldReturn200AndEmptyContent() throws Exception {
            // Given
            Information expectedInformation = new Information();
            expectedInformation.updateContent("");

            when(projectInformationService.findContentByProjectId(eq(testProjectId), eq(testUserId)))
                    .thenReturn(expectedInformation);

            // When & Then
            mockMvc.perform(get(API_BASE_URL + "/projects/{projectId}/information", testProjectId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            )))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value(""));

            verify(projectInformationService).findContentByProjectId(eq(testProjectId), eq(testUserId));
        }
    }

    @Nested
    @DisplayName("PUT /projects/{projectId}/information")
    class UpdateInformationTests {

        @Test
        @DisplayName("Should return 200 OK and updated information")
        void updateContent_whenValidRequest_shouldReturn200AndUpdatedInformation() throws Exception {
            // Given
            UpdateInformationContentRequest request = new UpdateInformationContentRequest("Updated content");

            Information expectedInformation = new Information();
            expectedInformation.updateContent("Updated content");

            when(projectInformationService.updateContent(eq(testProjectId), eq(testUserId), any(UpdateInformationContentRequest.class)))
                    .thenReturn(expectedInformation);

            // When & Then
            mockMvc.perform(put(API_BASE_URL + "/projects/{projectId}/information", testProjectId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            ))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value("Updated content"));

            verify(projectInformationService).updateContent(eq(testProjectId), eq(testUserId), any(UpdateInformationContentRequest.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when content exceeds maximum length")
        void updateContent_whenContentTooLong_shouldReturn400() throws Exception {
            // Given
            String longContent = "x".repeat(100001); // Exceeds max length
            UpdateInformationContentRequest request = new UpdateInformationContentRequest(longContent);

            // When & Then
            mockMvc.perform(put(API_BASE_URL + "/projects/{projectId}/information", testProjectId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            ))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(projectInformationService);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when content is null")
        void updateContent_whenContentIsNull_shouldReturn400() throws Exception {
            // Given
            String requestBody = "{\"content\": null}";

            // When & Then
            mockMvc.perform(put(API_BASE_URL + "/projects/{projectId}/information", testProjectId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            ))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(projectInformationService);
        }
    }

    @Nested
    @DisplayName("GET /projects/{projectId}/links")
    class GetLinksTests {

        @Test
        @DisplayName("Should return 200 OK and list of links")
        void findLinksByProjectId_whenValidRequest_shouldReturn200AndLinks() throws Exception {
            // Given
            Link link1 = Link.create("Documentation", "https://docs.example.com", LinkType.DOCS, null);
            Link link2 = Link.create("Repository", "https://github.com/user/repo", LinkType.GITHUB, null);
            Set<Link> expectedLinks = Set.of(link1, link2);

            when(projectInformationService.findLinksByProjectId(eq(testProjectId), eq(testUserId)))
                    .thenReturn(expectedLinks);

            // When & Then
            mockMvc.perform(get(API_BASE_URL + "/projects/{projectId}/links", testProjectId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            )))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].title", containsInAnyOrder("Documentation", "Repository")))
                    .andExpect(jsonPath("$[*].type", containsInAnyOrder("DOCS", "GITHUB")));

            verify(projectInformationService).findLinksByProjectId(eq(testProjectId), eq(testUserId));
        }
    }

    @Nested
    @DisplayName("POST /projects/{projectId}/links")
    class AddLinkTests {

        @Test
        @DisplayName("Should return 201 Created and added link")
        void addLink_whenValidRequest_shouldReturn201AndLink() throws Exception {
            // Given
            CreateLinkRequest request = new CreateLinkRequest("Documentation", "https://docs.example.com", LinkType.DOCS);

            Link expectedLink = Link.create("Documentation", "https://docs.example.com", LinkType.DOCS, null);

            when(projectInformationService.addLink(eq(testProjectId), eq(testUserId), any(CreateLinkRequest.class)))
                    .thenReturn(expectedLink);

            // When & Then
            mockMvc.perform(post(API_BASE_URL + "/projects/{projectId}/links", testProjectId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            ))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Documentation"))
                    .andExpect(jsonPath("$.url").value("https://docs.example.com"))
                    .andExpect(jsonPath("$.type").value("DOCS"));

            verify(projectInformationService).addLink(eq(testProjectId), eq(testUserId), any(CreateLinkRequest.class));
        }

        @Test
        @DisplayName("Should handle all link types correctly")
        void addLink_whenDifferentLinkTypes_shouldReturn201() throws Exception {
            // Test for each LinkType
            for (LinkType linkType : LinkType.values()) {
                CreateLinkRequest request = new CreateLinkRequest(
                        "Link " + linkType.name(),
                        "https://" + linkType.name().toLowerCase() + ".com",
                        linkType
                );

                Link expectedLink = Link.create(request.getTitle(), request.getUrl(), request.getLinkType(), null);

                when(projectInformationService.addLink(eq(testProjectId), eq(testUserId), any(CreateLinkRequest.class)))
                        .thenReturn(expectedLink);

                mockMvc.perform(post(API_BASE_URL + "/projects/{projectId}/links", testProjectId)
                                .with(jwt().jwt(jwt -> jwt
                                        .claim("sub", testUserId)
                                        .claim("preferred_username", testUsername)
                                ))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.type").value(linkType.name()));

                verify(projectInformationService).addLink(eq(testProjectId), eq(testUserId), any(CreateLinkRequest.class));
                reset(projectInformationService);
            }
        }

        @Test
        @DisplayName("Should return 400 Bad Request when title is empty")
        void addLink_whenTitleIsEmpty_shouldReturn400() throws Exception {
            // Given
            CreateLinkRequest request = new CreateLinkRequest("", "https://example.com", LinkType.WEB);

            // When & Then
            mockMvc.perform(post(API_BASE_URL + "/projects/{projectId}/links", testProjectId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            ))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(projectInformationService);
        }
    }

    @Nested
    @DisplayName("DELETE /projects/{projectId}/links/{linkId}")
    class DeleteLinkTests {

        @Test
        @DisplayName("Should return 204 No Content when link deleted successfully")
        void deleteLink_whenValidRequest_shouldReturn204() throws Exception {
            // Given
            doNothing().when(projectInformationService).deleteLink(eq(testProjectId), eq(testUserId), eq(testLinkId));

            // When & Then
            mockMvc.perform(delete(API_BASE_URL + "/projects/{projectId}/links/{linkId}", testProjectId, testLinkId)
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            )))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(projectInformationService).deleteLink(eq(testProjectId), eq(testUserId), eq(testLinkId));
        }

        @Test
        @DisplayName("Should handle invalid path parameters")
        void deleteLink_whenInvalidPathParameters_shouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(delete(API_BASE_URL + "/projects/{projectId}/links/{linkId}", "invalid", "invalid")
                            .with(jwt().jwt(jwt -> jwt
                                    .claim("sub", testUserId)
                                    .claim("preferred_username", testUsername)
                            )))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(projectInformationService);
        }
    }
}
