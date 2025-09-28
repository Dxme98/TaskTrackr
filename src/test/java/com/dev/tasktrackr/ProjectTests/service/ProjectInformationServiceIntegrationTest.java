package com.dev.tasktrackr.ProjectTests.service;

import com.dev.tasktrackr.BaseIntegrationTest;
import com.dev.tasktrackr.project.api.dtos.request.CreateLinkRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.project.domain.Information;
import com.dev.tasktrackr.project.domain.Link;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.enums.LinkType;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.service.ProjectInformationServiceImpl;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.LinkNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import com.dev.tasktrackr.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectInformationService Integration Tests")
public class ProjectInformationServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProjectInformationServiceImpl projectInformationService;

    private UserEntity ownerUser;
    private UserEntity anotherUser;
    private UserEntity nonMemberUser;
    private Project testProject;
    private ProjectMember ownerMember;
    private ProjectMember anotherMember;

    @BeforeEach
    void setUp() {
        ownerUser = createTestUser("user123", "testuser");
        anotherUser = createTestUser("user456", "anotheruser");
        nonMemberUser = createTestUser("user789", "nonmember");

        // Create project with test user as owner
        testProject = createTestProject("Test Project", ProjectType.BASIC, ownerUser);

        // Add additional member to project
        ownerMember = testProject.findProjectMember(ownerUser.getId());
        testProject.addMember(anotherUser);

        projectRepository.save(testProject);

        // Get actual saved members
        anotherMember = testProject.findProjectMember(anotherUser.getId());
    }

    @Nested
    @DisplayName("Update Content Tests")
    class UpdateContentTests {

        @Test
        @DisplayName("Should update information content as member with permission")
        @Rollback
        void shouldUpdateInformationContentAsMemberWithPermission() {
            // Given
            String updatedContent = "Content updated by base member";
            UpdateInformationContentRequest request = new UpdateInformationContentRequest(updatedContent);

            // When
            Information result = projectInformationService.updateContent(
                    testProject.getId(), ownerUser.getId(), request);

            // Then
            assertNotNull(result);
            assertEquals(updatedContent, result.getContent());
        }

        @Test
        @DisplayName("Should update large content successfully")
        @Rollback
        void shouldUpdateLargeContentSuccessfully() {
            // Given
            String largeContent = "Large content: " + "x".repeat(10000);
            UpdateInformationContentRequest request = new UpdateInformationContentRequest(largeContent);

            // When
            Information result = projectInformationService.updateContent(
                    testProject.getId(), ownerUser.getId(), request);

            // Then
            assertNotNull(result);
            assertEquals(largeContent, result.getContent());
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProject() {
            // Given
            UpdateInformationContentRequest request = new UpdateInformationContentRequest("Content");

            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> projectInformationService.updateContent(999L, ownerUser.getId(), request));
        }

        @Test
        @DisplayName("Should throw exception for non-member user")
        @Rollback
        void shouldThrowExceptionForNonMemberUser() {
            // Given
            UpdateInformationContentRequest request = new UpdateInformationContentRequest("Content");

            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> projectInformationService.updateContent(testProject.getId(), nonMemberUser.getId(), request));
        }
    }

    @Nested
    @DisplayName("Add Link Tests")
    class AddLinkTests {

        @Test
        @DisplayName("Should add repository link member with permission")
        @Rollback
        void shouldAddRepositoryLinkAsBaseMember() {
            // Given
            CreateLinkRequest request = new CreateLinkRequest(
                    "GitHub Repository",
                    "https://github.com/user/repo",
                    LinkType.GITHUB
            );

            // When
            Link result = projectInformationService.addLink(testProject.getId(), ownerUser.getId(), request);

            // Then
            assertNotNull(result);
            assertEquals("GitHub Repository", result.getTitle());
            assertEquals(LinkType.GITHUB, result.getType());
        }

        @Test
        @DisplayName("Should add multiple links successfully")
        @Rollback
        void shouldAddMultipleLinksSuccessfully() {
            // Given
            CreateLinkRequest docRequest = new CreateLinkRequest(
                    "Documentation", "https://docs.com", LinkType.DOCS);
            CreateLinkRequest repoRequest = new CreateLinkRequest(
                    "Repository", "https://repo.com", LinkType.GITHUB);
            CreateLinkRequest otherRequest = new CreateLinkRequest(
                    "Other Link", "https://other.com", LinkType.WEB);

            // When
            Link docLink = projectInformationService.addLink(testProject.getId(), ownerUser.getId(), docRequest);
            Link repoLink = projectInformationService.addLink(testProject.getId(), ownerUser.getId(), repoRequest);
            Link otherLink = projectInformationService.addLink(testProject.getId(), ownerUser.getId(), otherRequest);

            // Then
            assertNotNull(docLink);
            assertNotNull(repoLink);
            assertNotNull(otherLink);

            // Verify in database
            Project savedProject = projectRepository.findById(testProject.getId()).get();
            Set<Link> links = savedProject.getBasicDetails().getLinks();
            assertEquals(3, links.size());
        }

        @Test
        @DisplayName("Should handle all link types")
        @Rollback
        void shouldHandleAllLinkTypes() {
            // Given/When/Then
            for (LinkType linkType : LinkType.values()) {
                CreateLinkRequest request = new CreateLinkRequest(
                        "Link for " + linkType.name(),
                        "https://" + linkType.name().toLowerCase() + ".com",
                        linkType
                );

                Link result = projectInformationService.addLink(testProject.getId(), ownerUser.getId(), request);

                assertNotNull(result);
                assertEquals(linkType, result.getType());
            }

            // Verify all links were added
            Project savedProject = projectRepository.findById(testProject.getId()).get();
            assertEquals(LinkType.values().length, savedProject.getBasicDetails().getLinks().size());
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProjectWhenAdding() {
            // Given
            CreateLinkRequest request = new CreateLinkRequest("Link", "https://example.com", LinkType.WEB);

            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> projectInformationService.addLink(999L, ownerUser.getId(), request));
        }

        @Test
        @DisplayName("Should throw exception for non-member user")
        @Rollback
        void shouldThrowExceptionForNonMemberUserWhenAdding() {
            // Given
            CreateLinkRequest request = new CreateLinkRequest("Link", "https://example.com", LinkType.WEB);

            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> projectInformationService.addLink(testProject.getId(), nonMemberUser.getId(), request));
        }
    }

    @Nested
    @DisplayName("Delete Link Tests")
    class DeleteLinkTests {

        private Link testLink;

        @BeforeEach
        void setUpLink() {
            CreateLinkRequest request = new CreateLinkRequest(
                    "Test Link", "https://test.com", LinkType.DOCS);
            projectInformationService.addLink(testProject.getId(), ownerUser.getId(), request);

            projectRepository.save(testProject);

            testLink = testProject.getBasicDetails().getLinks().stream().findFirst().get();
        }

        @Test
        @DisplayName("Should delete link as Member with Permission")
        @Rollback
        void shouldDeleteLinkAsMemberWithPermission() {
            // Given
            assertEquals(1, testProject.getBasicDetails().getLinks().size());

            // When
            projectInformationService.deleteLink(testProject.getId(), ownerUser.getId(), testLink.getId());

            // Then
            // Verify in database
            Project savedProject = projectRepository.findById(testProject.getId()).get();
            assertEquals(0, savedProject.getBasicDetails().getLinks().size());
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent link")
        @Rollback
        void shouldThrowExceptionWhenDeletingNonExistentLink() {
            // When/Then
            assertThrows(LinkNotFoundException.class,
                    () -> projectInformationService.deleteLink(testProject.getId(), ownerUser.getId(), 999L));
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProjectWhenDeleting() {
            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> projectInformationService.deleteLink(999L, ownerUser.getId(), testLink.getId()));
        }

        @Test
        @DisplayName("Should throw exception for Member without Permission")
        @Rollback
        void shouldThrowExceptionForMemberWithoutPermission() {
            // When/Then
            assertThrows(PermissionDeniedException.class,
                    () -> projectInformationService.deleteLink(testProject.getId(), anotherUser.getId(), testLink.getId()));
        }
    }

    @Nested
    @DisplayName("Find Links Tests")
    class FindLinksTests {

        @BeforeEach
        void setUpLinks() {
            CreateLinkRequest docRequest = new CreateLinkRequest(
                    "Documentation", "https://docs.com", LinkType.DOCS);
            CreateLinkRequest repoRequest = new CreateLinkRequest(
                    "Repository", "https://repo.com", LinkType.GITHUB);

            projectInformationService.addLink(testProject.getId(), ownerUser.getId(), docRequest);
            projectInformationService.addLink(testProject.getId(), ownerUser.getId(), repoRequest);
        }

        @Test
        @DisplayName("Should find all links as base member")
        @Rollback
        void shouldFindAllLinksMember() {
            // When
            Set<Link> result = projectInformationService.findLinksByProjectId(testProject.getId(), anotherUser.getId());

            // Then
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProjectWhenFindingLinks() {
            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> projectInformationService.findLinksByProjectId(999L, ownerUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception for non-member user")
        @Rollback
        void shouldThrowExceptionForNonMemberUserWhenFindingLinks() {
            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> projectInformationService.findLinksByProjectId(testProject.getId(), nonMemberUser.getId()));
        }
    }

    @Nested
    @DisplayName("Find Content Tests")
    class FindContentTests {


        @Test
        @DisplayName("Should find  content as member")
        @Rollback
        void shouldFindContentAsBaseMember() {
            // Given - Update content first
            String updatedContent = "Updated project information";
            UpdateInformationContentRequest request = new UpdateInformationContentRequest(updatedContent);
            projectInformationService.updateContent(testProject.getId(), ownerUser.getId(), request);

            // When
            Information result = projectInformationService.findContentByProjectId(testProject.getId(), anotherUser.getId());

            // Then
            assertNotNull(result);
            assertEquals(updatedContent, result.getContent());
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProjectWhenFindingContent() {
            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> projectInformationService.findContentByProjectId(999L, ownerUser.getId()));
        }
    }
}

