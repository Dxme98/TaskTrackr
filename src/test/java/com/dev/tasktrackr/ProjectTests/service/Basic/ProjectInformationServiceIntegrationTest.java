package com.dev.tasktrackr.ProjectTests.service.Basic;

import com.dev.tasktrackr.ProjectTests.service.shared.BasicDetailsBaseTest;
import com.dev.tasktrackr.project.api.dtos.request.CreateLinkRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.basic.Information;
import com.dev.tasktrackr.project.domain.basic.Link;
import com.dev.tasktrackr.project.domain.basic.LinkType;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.repository.LinkRepository;
import com.dev.tasktrackr.project.repository.ProjectInformationRepository;
import com.dev.tasktrackr.project.repository.ProjectMemberRepository;
import com.dev.tasktrackr.project.service.ProjectInformationService;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.LinkNotFoundException;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectInformationService Integration Tests")
public class ProjectInformationServiceIntegrationTest extends BasicDetailsBaseTest {

    @Autowired
    private ProjectInformationService projectInformationService;

    @Autowired
    private ProjectInformationRepository projectInformationRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    private UserEntity ownerUser;
    private UserEntity anotherUser;
    private UserEntity nonMemberUser;
    private Project testProject;
    private ProjectMember ownerMember;
    private ProjectMember anotherMember;

    @BeforeEach
    void setUp() {
        ownerUser = testDataFactory.createTestUser("user123", "testuser");
        anotherUser = testDataFactory.createTestUser("user456", "anotheruser");
        nonMemberUser = testDataFactory.createTestUser("user789", "nonmember");

        testProject = testDataFactory.createTestProject("Test Project", ProjectType.BASIC, ownerUser);

        ownerMember = projectMemberRepository.findProjectMemberByUserIdAndProjectId(ownerUser.getId(), testProject.getId())
                .orElseThrow(() -> new IllegalStateException("Owner-Mitglied wurde nicht korrekt erstellt."));
        anotherMember = testDataFactory.createTestMember(testProject, anotherUser);
    }

    @Nested
    @DisplayName("Update Content Tests")
    class UpdateContentTests {

        @Test
        @DisplayName("Should update information content as member with permission")
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

            // Verify in database
            Information savedInfo = projectInformationRepository.findByBasicDetailsId(testProject.getId()).orElseThrow();
            assertEquals(updatedContent, savedInfo.getContent());
        }

        @Test
        @DisplayName("Should update large content successfully")
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
        @DisplayName("Should throw exception for non-member user")
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
        void shouldAddRepositoryLinkAsBaseMember() {
            // Given
            CreateLinkRequest request = new CreateLinkRequest(
                    "GitHub Repository",
                    "https://github.com/user/repo",
                    LinkType.GITHUB
            );
            long initialLinkCount = linkRepository.count();

            // When
            Link result = projectInformationService.addLink(testProject.getId(), ownerUser.getId(), request);

            // Then
            assertNotNull(result);
            assertEquals("GitHub Repository", result.getTitle());
            assertEquals(LinkType.GITHUB, result.getType());
            assertEquals(initialLinkCount + 1, linkRepository.count());
        }

        @Test
        @DisplayName("Should add multiple links successfully")
        void shouldAddMultipleLinksSuccessfully() {
            // Given
            CreateLinkRequest docRequest = new CreateLinkRequest(
                    "Documentation", "https://docs.com", LinkType.DOCS);
            CreateLinkRequest repoRequest = new CreateLinkRequest(
                    "Repository", "https://repo.com", LinkType.GITHUB);
            CreateLinkRequest otherRequest = new CreateLinkRequest(
                    "Other Link", "https://other.com", LinkType.WEB);

            // When
            projectInformationService.addLink(testProject.getId(), ownerUser.getId(), docRequest);
            projectInformationService.addLink(testProject.getId(), ownerUser.getId(), repoRequest);
            projectInformationService.addLink(testProject.getId(), ownerUser.getId(), otherRequest);

            // Then
            // Verify in database
            Set<Link> links = linkRepository.findAllByBasicDetailsId(testProject.getId());
            assertEquals(3, links.size());
        }

        @Test
        @DisplayName("Should throw exception for non-member user")
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
            testLink = projectInformationService.addLink(testProject.getId(), ownerUser.getId(), request);
        }

        @Test
        @DisplayName("Should delete link as Member with Permission")
        void shouldDeleteLinkAsMemberWithPermission() {
            // Given
            long initialLinkCount = linkRepository.count(); // 1
            assertEquals(1, initialLinkCount);

            // When
            projectInformationService.deleteLink(testProject.getId(), ownerUser.getId(), testLink.getId());

            // Then
            // Verify in database
            assertEquals(0, linkRepository.count());
            assertFalse(linkRepository.existsById(testLink.getId()));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent link")
        void shouldThrowExceptionWhenDeletingNonExistentLink() {
            // When/Then
            assertThrows(LinkNotFoundException.class,
                    () -> projectInformationService.deleteLink(testProject.getId(), ownerUser.getId(), 999L));
        }

        @Test
        @DisplayName("Should throw exception for Member without Permission")
        void shouldThrowExceptionForMemberWithoutPermission() {
            // When/Then
            // anotherUser has BASE role, which lacks BASIC_EDIT_INFORMATION
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
        void shouldFindAllLinksMember() {
            // When
            Set<Link> result = projectInformationService.findLinksByProjectId(testProject.getId(), anotherUser.getId());

            // Then
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should throw exception for non-member user")
        void shouldThrowExceptionForNonMemberUserWhenFindingLinks() {
            // When/Then
            // Service checks membership, throws ProjectMemberNotFoundException
            assertThrows(UserNotProjectMemberException.class,
                    () -> projectInformationService.findLinksByProjectId(testProject.getId(), nonMemberUser.getId()));
        }
    }

    @Nested
    @DisplayName("Find Content Tests")
    class FindContentTests {

        @Test
        @DisplayName("Should find content as member")
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
    }
}