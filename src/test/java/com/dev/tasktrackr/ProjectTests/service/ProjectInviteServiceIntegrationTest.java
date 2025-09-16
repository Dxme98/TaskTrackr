package com.dev.tasktrackr.ProjectTests.service;

import com.dev.tasktrackr.BaseIntegrationTest;
import com.dev.tasktrackr.project.api.dtos.request.ProjectInviteRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.repository.ProjectInviteQueryRepository;
import com.dev.tasktrackr.project.service.ProjectInviteServiceImpl;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectInviteNotFound;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UnauthorizedInviteHandleAcception;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.UserNotFoundException;
import com.dev.tasktrackr.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectInviteService Integration Tests")
public class ProjectInviteServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProjectInviteServiceImpl projectInviteService;

    @Autowired
    private ProjectInviteQueryRepository projectInviteQueryRepository;

    private UserEntity ownerUser;
    private UserEntity memberUser;
    private UserEntity inviteeUser;
    private UserEntity nonMemberUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        ownerUser = createTestUser("owner123", "owner");
        memberUser = createTestUser("member456", "member");
        inviteeUser = createTestUser("invitee789", "invitee");
        nonMemberUser = createTestUser("nonmember999", "nonmember");

        testProject = createTestProject("Test Project", ProjectType.BASIC, ownerUser);
        testProject.addMember(memberUser);
        projectRepository.save(testProject);
    }

    @Nested
    @DisplayName("Create Project Invite Tests")
    class CreateProjectInviteTests {

        @Test
        @DisplayName("Should create project invite successfully as owner")
        @Rollback
        void shouldCreateProjectInviteSuccessfully() {
            // Given
            ProjectInviteRequest request = new ProjectInviteRequest();
            request.setReceiverUsername(inviteeUser.getUsername());

            // When
            ProjectInviteResponseDto result = assertDoesNotThrow(() ->
                    projectInviteService.createProjectInvite(request, ownerUser.getId(), testProject.getId()));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSenderId()).isEqualTo(ownerUser.getId());
            assertThat(result.getSenderUsername()).isEqualTo(ownerUser.getUsername());
            assertThat(result.getReceiverId()).isEqualTo(inviteeUser.getId());
            assertThat(result.getReceiverUsername()).isEqualTo(inviteeUser.getUsername());
            assertThat(result.getProjectId()).isEqualTo(testProject.getId());
            assertThat(result.getProjectName()).isEqualTo(testProject.getName());
            assertThat(result.getInviteStatus()).isEqualTo(ProjectInviteStatus.PENDING);
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();

            // Verify invite is persisted
            Project savedProject = projectRepository.findProjectWithInvitesAndMember(testProject.getId()).get();
            assertThat(savedProject.getProjectInvites().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw exception when member without permission tries to create invite")
        @Rollback
        void shouldThrowExceptionWhenMemberWithoutPermissionTriesToCreateInvite() {
            // Given
            ProjectInviteRequest request = new ProjectInviteRequest();
            request.setReceiverUsername(inviteeUser.getUsername());

            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.createProjectInvite(request, memberUser.getId(), testProject.getId()))
                    .isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        @Rollback
        void shouldThrowExceptionForNonExistentProject() {
            // Given
            ProjectInviteRequest request = new ProjectInviteRequest();
            request.setReceiverUsername(inviteeUser.getUsername());

            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.createProjectInvite(request, ownerUser.getId(), 9999L))
                    .isInstanceOf(ProjectNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception for non-existent sender")
        @Rollback
        void shouldThrowExceptionForNonExistentSender() {
            // Given
            ProjectInviteRequest request = new ProjectInviteRequest();
            request.setReceiverUsername(inviteeUser.getUsername());

            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.createProjectInvite(request, "nonexistent", testProject.getId()))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception for non-existent receiver")
        @Rollback
        void shouldThrowExceptionForNonExistentReceiver() {
            // Given
            ProjectInviteRequest request = new ProjectInviteRequest();
            request.setReceiverUsername("nonexistent");

            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.createProjectInvite(request, ownerUser.getId(), testProject.getId()))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Accept Project Invite Tests")
    class AcceptProjectInviteTests {

        private ProjectInvite createTestInvite() {
            ProjectInviteRequest request = new ProjectInviteRequest();
            request.setReceiverUsername(inviteeUser.getUsername());
            projectInviteService.createProjectInvite(request, ownerUser.getId(), testProject.getId());

            Project project = projectRepository.findProjectWithInvitesAndMember(testProject.getId()).get();
            return project.getProjectInvites().get(0);
        }

        @Test
        @DisplayName("Should accept project invite successfully")
        @Rollback
        void shouldAcceptProjectInviteSuccessfully() {
            // Given
            ProjectInvite invite = createTestInvite();
            int initialMemberCount = testProject.getProjectMembers().size();

            // When
            ProjectInviteResponseDto result = assertDoesNotThrow(() ->
                    projectInviteService.acceptProjectInvite(inviteeUser.getId(), invite.getId()));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getInviteStatus()).isEqualTo(ProjectInviteStatus.ACCEPTED);

            // Verify user is now a project member
            Project savedProject = projectRepository.findProjectWithInvitesAndMember(testProject.getId()).get();
            assertThat(savedProject.getProjectMembers().size()).isEqualTo(initialMemberCount + 1);

            boolean userIsMember = savedProject.getProjectMembers().stream()
                    .anyMatch(member -> member.getUser().getId().equals(inviteeUser.getId()));
            assertThat(userIsMember).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when wrong user tries to accept invite")
        @Rollback
        void shouldThrowExceptionWhenWrongUserTriesToAcceptInvite() {
            // Given
            ProjectInvite invite = createTestInvite();

            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.acceptProjectInvite(nonMemberUser.getId(), invite.getId()))
                    .isInstanceOf(UnauthorizedInviteHandleAcception.class);
        }

        @Test
        @DisplayName("Should throw exception for non-existent invite")
        @Rollback
        void shouldThrowExceptionForNonExistentInvite() {
            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.acceptProjectInvite(inviteeUser.getId(), 9999L))
                    .isInstanceOf(ProjectInviteNotFound.class);
        }
    }

    @Nested
    @DisplayName("Decline Project Invite Tests")
    class DeclineProjectInviteTests {

        private ProjectInvite createTestInvite() {
            ProjectInviteRequest request = new ProjectInviteRequest();
            request.setReceiverUsername(inviteeUser.getUsername());
            projectInviteService.createProjectInvite(request, ownerUser.getId(), testProject.getId());

            Project project = projectRepository.findProjectWithInvitesAndMember(testProject.getId()).get();
            return project.getProjectInvites().get(0);
        }

        @Test
        @DisplayName("Should decline project invite successfully")
        @Rollback
        void shouldDeclineProjectInviteSuccessfully() {
            // Given
            ProjectInvite invite = createTestInvite();
            int initialMemberCount = testProject.getProjectMembers().size();

            // When
            ProjectInviteResponseDto result = assertDoesNotThrow(() ->
                    projectInviteService.declineProjectInvite(inviteeUser.getId(), invite.getId()));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getInviteStatus()).isEqualTo(ProjectInviteStatus.DECLINED);

            // Verify user is NOT a project member
            Project savedProject = projectRepository.findProjectWithInvitesAndMember(testProject.getId()).get();
            assertThat(savedProject.getProjectMembers().size()).isEqualTo(initialMemberCount);

            boolean userIsMember = savedProject.getProjectMembers().stream()
                    .anyMatch(member -> member.getUser().getId().equals(inviteeUser.getId()));
            assertThat(userIsMember).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when wrong user tries to decline invite")
        @Rollback
        void shouldThrowExceptionWhenWrongUserTriesToDeclineInvite() {
            // Given
            ProjectInvite invite = createTestInvite();

            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.declineProjectInvite(nonMemberUser.getId(), invite.getId()))
                    .isInstanceOf(UnauthorizedInviteHandleAcception.class);
        }

        @Test
        @DisplayName("Should throw exception for non-existent invite")
        @Rollback
        void shouldThrowExceptionForNonExistentInvite() {
            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.declineProjectInvite(inviteeUser.getId(), 9999L))
                    .isInstanceOf(ProjectInviteNotFound.class);
        }
    }

    @Nested
    @DisplayName("Find Pending Invites Tests")
    class FindPendingInvitesTests {

        private void createMultipleInvites() {
            // Create pending invite
            ProjectInviteRequest request1 = new ProjectInviteRequest();
            request1.setReceiverUsername(inviteeUser.getUsername());
            projectInviteService.createProjectInvite(request1, ownerUser.getId(), testProject.getId());

            // Create another project and invite
            Project anotherProject = createTestProject("Another Project", ProjectType.BASIC, memberUser);
            ProjectInviteRequest request2 = new ProjectInviteRequest();
            request2.setReceiverUsername(inviteeUser.getUsername());
            projectInviteService.createProjectInvite(request2, memberUser.getId(), anotherProject.getId());
        }

        @Test
        @DisplayName("Should find all pending invites for user with pagination")
        @Rollback
        void shouldFindAllPendingInvitesForUserWithPagination() {
            // Given
            createMultipleInvites();
            PageRequest pageRequest = PageRequest.of(0, 10);

            // When
            Page<ProjectInviteResponseDto> result = projectInviteService.findAllPendingInvitesByUserId(
                    inviteeUser.getId(), pageRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent().size()).isEqualTo(2);
            assertThat(result.getTotalElements()).isEqualTo(2);

            // Verify all invites are pending
            assertThat(result.getContent().get(0).getInviteStatus()).isEqualTo(ProjectInviteStatus.PENDING);
            assertThat(result.getContent().get(1).getInviteStatus()).isEqualTo(ProjectInviteStatus.PENDING);

            // Verify receiver is correct
            assertEquals(result.getContent().get(0).getReceiverId(), inviteeUser.getId());
            assertEquals(result.getContent().get(1).getReceiverId(), inviteeUser.getId());
        }

        @Test
        @DisplayName("Should return empty page when user has no pending invites")
        @Rollback
        void shouldReturnEmptyPageWhenUserHasNoPendingInvites() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10);

            // When
            Page<ProjectInviteResponseDto> result = projectInviteService.findAllPendingInvitesByUserId(
                    nonMemberUser.getId(), pageRequest);

            // Then
            assertThat(result).isNotNull();
            assertTrue(result.getContent().isEmpty());
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should not return accepted or declined invites")
        @Rollback
        void shouldNotReturnAcceptedOrDeclinedInvites() {
            // Given
            createMultipleInvites();

            // Accept one invite
            Project project = projectRepository.findProjectWithInvitesAndMember(testProject.getId()).get();
            ProjectInvite firstInvite = project.getProjectInvites().get(0);
            projectInviteService.acceptProjectInvite(inviteeUser.getId(), firstInvite.getId());

            PageRequest pageRequest = PageRequest.of(0, 10);

            // When
            Page<ProjectInviteResponseDto> result = projectInviteService.findAllPendingInvitesByUserId(
                    inviteeUser.getId(), pageRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent().size()).isEqualTo(1); // Only the pending one
            assertThat(result.getTotalElements()).isEqualTo(1);

            assertThat(result.getContent().get(0).getInviteStatus())
                    .isEqualTo(ProjectInviteStatus.PENDING);
        }
    }
}
