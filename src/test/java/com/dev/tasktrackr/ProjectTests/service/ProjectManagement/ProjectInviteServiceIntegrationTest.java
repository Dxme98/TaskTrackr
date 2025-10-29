package com.dev.tasktrackr.ProjectTests.service.ProjectManagement;

import com.dev.tasktrackr.ProjectTests.service.shared.ProjectManagementBaseTest;
import com.dev.tasktrackr.project.api.dtos.request.ProjectInviteRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.service.ProjectInviteService;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.InviteIsNotPendingException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UnauthorizedInviteHandleAcception;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.ProjectInviteAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.UserAlreadyPartOfProjectException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectInviteNotFound;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.UserNotFoundException;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectInviteService Integration Tests")
public class ProjectInviteServiceIntegrationTest extends ProjectManagementBaseTest {

    @Autowired
    private ProjectInviteService projectInviteService;


    private UserEntity ownerUser;
    private UserEntity memberUser;
    private UserEntity inviteeUser;
    private UserEntity nonMemberUser;
    private Project testProject;
    private ProjectMember baseMember; // Mitglied mit BASE-Rolle für Permission-Tests

    @BeforeEach
    void setUp() {
        ownerUser = createTestUser("owner123", "owner");
        memberUser = createTestUser("member456", "member");
        inviteeUser = createTestUser("invitee789", "invitee");
        nonMemberUser = createTestUser("nonmember999", "nonmember");

        testProject = createTestProject("Test Project", ProjectType.BASIC, ownerUser);

        baseMember = createTestMember(testProject, memberUser);
    }

    @Nested
    @DisplayName("Create Project Invite Tests")
    class CreateProjectInviteTests {

        private ProjectInviteRequest createRequest(String username) {
            ProjectInviteRequest request = new ProjectInviteRequest();
            request.setReceiverUsername(username);
            return request;
        }

        @Test
        @DisplayName("Should create project invite successfully as owner")
        void shouldCreateProjectInviteSuccessfully() {
            // Given
            ProjectInviteRequest request = createRequest(inviteeUser.getUsername());
            long initialInviteCount = projectInviteRepository.count();

            // When
            ProjectInviteResponseDto result = assertDoesNotThrow(() ->
                    projectInviteService.createProjectInvite(request, ownerUser.getId(), testProject.getId()));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSenderId()).isEqualTo(ownerUser.getId());
            assertThat(result.getReceiverId()).isEqualTo(inviteeUser.getId());
            assertThat(result.getProjectId()).isEqualTo(testProject.getId());
            assertThat(result.getInviteStatus()).isEqualTo(ProjectInviteStatus.PENDING);
            assertThat(result.getCreatedAt()).isNotNull();

            // Verify invite is persisted
            assertThat(projectInviteRepository.count()).isEqualTo(initialInviteCount + 1);
        }

        @Test
        @DisplayName("Should throw exception when member without permission tries to create invite")
        void shouldThrowExceptionWhenMemberWithoutPermissionTriesToCreateInvite() {
            // Given
            ProjectInviteRequest request = createRequest(inviteeUser.getUsername());

            // When & Then
            // baseMember hat die BASE-Rolle, die keine COMMON_INVITE_USER-Rechte hat
            assertThatThrownBy(() ->
                    projectInviteService.createProjectInvite(request, baseMember.getUser().getId(), testProject.getId()))
                    .isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        @DisplayName("Should throw exception if user is already a member")
        void shouldThrowExceptionIfUserIsAlreadyAMember() {
            // Given
            ProjectInviteRequest request = createRequest(memberUser.getUsername()); // memberUser ist bereits Mitglied

            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.createProjectInvite(request, ownerUser.getId(), testProject.getId()))
                    .isInstanceOf(UserAlreadyPartOfProjectException.class);
        }

        @Test
        @DisplayName("Should throw exception if invite for user is already pending")
        void shouldThrowExceptionIfInviteIsAlreadyPending() {
            // Given
            ProjectInviteRequest request = createRequest(inviteeUser.getUsername());
            createTestInvite(testProject, ownerUser, inviteeUser); // Ersten Invite erstellen

            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.createProjectInvite(request, ownerUser.getId(), testProject.getId()))
                    .isInstanceOf(ProjectInviteAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        void shouldThrowExceptionForNonExistentProject() {
            // Given
            ProjectInviteRequest request = createRequest(inviteeUser.getUsername());

            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.createProjectInvite(request, ownerUser.getId(), 9999L))
                    .isInstanceOf(ProjectNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception for non-existent receiver")
        void shouldThrowExceptionForNonExistentReceiver() {
            // Given
            ProjectInviteRequest request = createRequest("nonexistent.user");

            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.createProjectInvite(request, ownerUser.getId(), testProject.getId()))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Accept Project Invite Tests")
    class AcceptProjectInviteTests {

        private ProjectInvite pendingInvite;

        @BeforeEach
        void setUpInvite() {
            pendingInvite = createTestInvite(testProject, ownerUser, inviteeUser);
        }

        @Test
        @DisplayName("Should accept project invite successfully")
        void shouldAcceptProjectInviteSuccessfully() {
            // Given
            long initialMemberCount = projectMemberRepository.count(); // Sollte 2 sein (owner + memberUser)

            // When
            ProjectInviteResponseDto result = assertDoesNotThrow(() ->
                    projectInviteService.acceptProjectInvite(inviteeUser.getId(), pendingInvite.getId()));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getInviteStatus()).isEqualTo(ProjectInviteStatus.ACCEPTED);

            // Verify user is now a project member
            assertThat(projectMemberRepository.count()).isEqualTo(initialMemberCount + 1);

            boolean userIsMember = projectMemberRepository.existsByUserIdAndProjectId(inviteeUser.getId(), testProject.getId());
            assertThat(userIsMember).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when wrong user tries to accept invite")
        void shouldThrowExceptionWhenWrongUserTriesToAcceptInvite() {
            // When & Then
            // nonMemberUser versucht, die Einladung von inviteeUser anzunehmen
            assertThatThrownBy(() ->
                    projectInviteService.acceptProjectInvite(nonMemberUser.getId(), pendingInvite.getId()))
                    .isInstanceOf(UnauthorizedInviteHandleAcception.class);
        }

        @Test
        @DisplayName("Should throw exception when accepting non-pending invite")
        void shouldThrowExceptionWhenAcceptingNonPendingInvite() {
            // Given
            // Einladung annehmen
            projectInviteService.acceptProjectInvite(inviteeUser.getId(), pendingInvite.getId());

            // When & Then
            // Erneut versuchen, anzunehmen
            assertThatThrownBy(() ->
                    projectInviteService.acceptProjectInvite(inviteeUser.getId(), pendingInvite.getId()))
                    .isInstanceOf(InviteIsNotPendingException.class); // Aus invite.accept(receiverId)
        }

        @Test
        @DisplayName("Should throw exception for non-existent invite")
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

        private ProjectInvite pendingInvite;

        @BeforeEach
        void setUpInvite() {
            pendingInvite = createTestInvite(testProject, ownerUser, inviteeUser);
        }

        @Test
        @DisplayName("Should decline and delete project invite successfully")
        void shouldDeclineAndRemoveProjectInviteSuccessfully() {
            // Given
            long initialInviteCount = projectInviteRepository.count();
            long initialMemberCount = projectMemberRepository.count();

            // When
            assertDoesNotThrow(() ->
                    projectInviteService.declineProjectInvite(inviteeUser.getId(), pendingInvite.getId()));

            // Then
            // Verify invite is deleted
            assertThat(projectInviteRepository.count()).isEqualTo(initialInviteCount - 1);

            // Verify user is NOT a project member
            assertThat(projectMemberRepository.count()).isEqualTo(initialMemberCount);
            boolean userIsMember = projectMemberRepository.existsByUserIdAndProjectId(inviteeUser.getId(), testProject.getId());
            assertThat(userIsMember).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when wrong user tries to decline invite")
        void shouldThrowExceptionWhenWrongUserTriesToDeclineInvite() {
            // When & Then
            assertThatThrownBy(() ->
                    projectInviteService.declineProjectInvite(nonMemberUser.getId(), pendingInvite.getId()))
                    .isInstanceOf(UnauthorizedInviteHandleAcception.class);
        }
    }

    @Nested
    @DisplayName("Find Pending Invites Tests")
    class FindPendingInvitesTests {

        @Test
        @DisplayName("Should find all pending invites for user with pagination")
        void shouldFindAllPendingInvitesForUserWithPagination() {
            // Given
            Project anotherProject = createTestProject("Another Project", ProjectType.BASIC, ownerUser);
            createTestInvite(testProject, ownerUser, inviteeUser);
            createTestInvite(anotherProject, ownerUser, inviteeUser);

            PageRequest pr = PageRequest.of(0, 10);

            // When
            Page<ProjectInviteResponseDto> result = projectInviteService.findAllPendingInvitesByUserId(
                    inviteeUser.getId(), pr);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(ProjectInviteResponseDto::getInviteStatus)
                    .allMatch(status -> status == ProjectInviteStatus.PENDING);
        }

        @Test
        @DisplayName("Should return empty page when user has no pending invites")
        void shouldReturnEmptyPageWhenUserHasNoPendingInvites() {
            // Given
            PageRequest pr = PageRequest.of(0, 10);

            // When
            Page<ProjectInviteResponseDto> result = projectInviteService.findAllPendingInvitesByUserId(
                    nonMemberUser.getId(), pr);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }
}