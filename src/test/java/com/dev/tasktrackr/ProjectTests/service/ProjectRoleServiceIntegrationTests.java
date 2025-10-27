package com.dev.tasktrackr.ProjectTests.service;

import com.dev.tasktrackr.BaseIntegrationTest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.request.CreateProjectRoleRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.service.ProjectRoleServiceImpl;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleAssignmentException;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleDeletion;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.RoleNameAlreadyExistsException;
import com.dev.tasktrackr.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

/**
@DisplayName("ProjectRoleService Integration Tests")
public class ProjectRoleServiceIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private ProjectRoleServiceImpl projectRoleService;

    private UserEntity ownerUser;
    private UserEntity regularUser;
    private UserEntity nonMemberUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        ownerUser = createTestUser("owner123", "owner");
        regularUser = createTestUser("regular456", "regular");
        nonMemberUser = createTestUser("nonmember789", "nonmember");

        testProject = createTestProject("Test Project", ProjectType.BASIC, ownerUser);
        testProject.addMember(regularUser);
        projectRepository.save(testProject);
    }

    @Nested
    @DisplayName("Create Project Role Tests")
    class CreateProjectRoleTests {

        @Test
        @DisplayName("Should create custom role successfully")
        @Rollback
        void shouldCreateCustomRoleSuccessfully() {
            // Given
            Set<PermissionName> permissions = EnumSet.of(
                    PermissionName.BASIC_CREATE_TASK,
                    PermissionName.COMMON_INVITE_USER,
                    PermissionName.COMMON_MANAGE_ROLES
            );
            CreateProjectRoleRequest request = new CreateProjectRoleRequest();
            request.setName("Developer");
            request.setPermissions(permissions);

            // When
            ProjectRoleResponse result = projectRoleService.createProjectRole(
                    ownerUser.getId(),
                    request,
                    testProject.getId()
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Developer");
            assertThat(result.getRoleType()).isEqualTo(RoleType.CUSTOM);
            assertThat(result.getPermissions()).containsExactlyInAnyOrderElementsOf(permissions);

            // Verify in database
            Project savedProject = projectRepository.findProjectWithRoles(testProject.getId()).get();
            assertThat(savedProject.getProjectRoles()).hasSize(3); // OWNER + BASE + Developer
        }

        @Test
        @DisplayName("Should throw exception when User without permission tries to create role")
        @Rollback
        void shouldThrowExceptionWhenUserWithoutPermissionTriesToCreateRole() {
            // Given
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            CreateProjectRoleRequest request = new CreateProjectRoleRequest();
            request.setName("Unauthorized Role");
            request.setPermissions(permissions);

            // When & Then
            assertThatThrownBy(() -> projectRoleService.createProjectRole(
                    regularUser.getId(),
                    request,
                    testProject.getId()
            )).isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        @DisplayName("Should throw exception for duplicate role name")
        @Rollback
        void shouldThrowExceptionForDuplicateRoleName() {
            // Given
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            CreateProjectRoleRequest request = new CreateProjectRoleRequest();
            request.setName("OWNER"); // Existing role name
            request.setPermissions(permissions);

            // When & Then
            assertThatThrownBy(() -> projectRoleService.createProjectRole(
                    ownerUser.getId(),
                    request,
                    testProject.getId()
            )).isInstanceOf(RoleNameAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Should throw exception for invalid permissions")
        @Rollback
        void shouldThrowExceptionForInvalidPermissions() {
            // Given - SCRUM permissions for BASIC project
            Set<PermissionName> invalidPermissions = EnumSet.of(
                    PermissionName.SCRUM_CREATE_USER_STORY
            );
            CreateProjectRoleRequest request = new CreateProjectRoleRequest();
            request.setName("Invalid Role");
            request.setPermissions(invalidPermissions);

            // When & Then
            assertThatThrownBy(() -> projectRoleService.createProjectRole(
                    ownerUser.getId(),
                    request,
                    testProject.getId()
            )).isInstanceOf(InvalidRoleAssignmentException.class);
        }

        @Test
        @DisplayName("Should persist role with correct ID generation")
        @Rollback
        void shouldPersistRoleWithCorrectIdGeneration() {
            // Given
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            CreateProjectRoleRequest request = new CreateProjectRoleRequest();
            request.setName("ID Test Role");
            request.setPermissions(permissions);

            // When
            ProjectRoleResponse result = projectRoleService.createProjectRole(
                    ownerUser.getId(),
                    request,
                    testProject.getId()
            );

            // Then
            assertThat(result.getId()).isNotNull();
            assertThat(result.getId()).isGreaterThan(0);

            // Verify ID is actually persisted
            Project savedProject = projectRepository.findProjectWithRoles(testProject.getId()).get();
            boolean roleExists = savedProject.getProjectRoles().stream()
                    .anyMatch(role -> role.getId() == result.getId());
            assertThat(roleExists).isTrue();
        }
    }

    @Nested
    @DisplayName("Delete Project Role Tests")
    class DeleteProjectRoleTests {

        private int customRoleId;

        @BeforeEach
        void setUpCustomRole() {
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            CreateProjectRoleRequest request = new CreateProjectRoleRequest();
            request.setName("Deletable Role");
            request.setPermissions(permissions);

            ProjectRoleResponse createdRole = projectRoleService.createProjectRole(
                    ownerUser.getId(),
                    request,
                    testProject.getId()
            );
            customRoleId = createdRole.getId();;
        }

        @Test
        @DisplayName("Should delete custom role successfully")
        @Rollback
        void shouldDeleteCustomRoleSuccessfully() {
            // When
            assertDoesNotThrow(() -> projectRoleService.deleteProjectRole(
                    ownerUser.getId(),
                    testProject.getId(),
                    customRoleId
            ));

            // Then
            Project savedProject = projectRepository.findProjectWithRoles(testProject.getId()).get();
            boolean roleExists = savedProject.getProjectRoles().stream()
                    .anyMatch(role -> role.getId() == customRoleId);
            assertThat(roleExists).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when deleting OWNER role")
        @Rollback
        void shouldThrowExceptionWhenDeletingOwnerRole() {
            // Given
            int ownerRoleId = testProject.getOwnerRole().getId();

            // When & Then
            assertThatThrownBy(() -> projectRoleService.deleteProjectRole(
                    ownerUser.getId(),
                    testProject.getId(),
                    ownerRoleId
            )).isInstanceOf(InvalidRoleDeletion.class);
        }

        @Test
        @DisplayName("Should throw exception when deleting BASE role")
        @Rollback
        void shouldThrowExceptionWhenDeletingBaseRole() {
            // Given
            int baseRoleId = testProject.getBaseRole().getId();

            // When & Then
            assertThatThrownBy(() -> projectRoleService.deleteProjectRole(
                    ownerUser.getId(),
                    testProject.getId(),
                    baseRoleId
            )).isInstanceOf(InvalidRoleDeletion.class);
        }

        @Test
        @DisplayName("Should throw exception when role is in use")
        @Rollback
        void shouldThrowExceptionWhenRoleIsInUse() {
            // Given - Assign custom role to regular user
            ProjectMember regularMember = testProject.getProjectMembers().stream()
                    .filter(member -> member.getUser().getId().equals(regularUser.getId()))
                    .findFirst().get();

            ProjectRole customRole = testProject.getProjectRoles().stream()
                    .filter(role -> role.getId() == customRoleId)
                    .findFirst().get();

            regularMember.assignRole(customRole);
            projectRepository.save(testProject);

            // When & Then
            assertThatThrownBy(() -> projectRoleService.deleteProjectRole(
                    ownerUser.getId(),
                    testProject.getId(),
                    customRoleId
            )).isInstanceOf(InvalidRoleDeletion.class)
                    .hasMessageContaining("Remove Role from ProjectMember before deleting");
        }
    }

    @Nested
    @DisplayName("Assign Role Tests")
    class AssignRoleTests {

        private int customRoleId;
        private Long regularMemberId;

        @BeforeEach
        void setUpRoleAndMember() {
            // Create custom role
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            CreateProjectRoleRequest request = new CreateProjectRoleRequest();
            request.setName("Assignable Role");
            request.setPermissions(permissions);

            ProjectRoleResponse createdRole = projectRoleService.createProjectRole(
                    ownerUser.getId(),
                    request,
                    testProject.getId()
            );
            customRoleId = createdRole.getId();

            // Get regular member ID
            regularMemberId = testProject.getProjectMembers().stream()
                    .filter(member -> member.getUser().getId().equals(regularUser.getId()))
                    .findFirst().get().getId();
        }

        @Test
        @DisplayName("Should assign role successfully")
        @Rollback
        void shouldAssignRoleSuccessfully() {
            // When
            ProjectMemberDto result = projectRoleService.assignRole(
                    ownerUser.getId(),
                    customRoleId,
                    regularMemberId,
                    testProject.getId()
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRole()).isEqualTo("Assignable Role");

            // Verify in database
            Project savedProject = projectRepository.findProjectWithInvitesAndMember(testProject.getId()).get();
            ProjectMember updatedMember = savedProject.getProjectMembers().stream()
                    .filter(member -> member.getId().equals(regularMemberId))
                    .findFirst().get();
            assertThat(updatedMember.getProjectRole().getId()).isEqualTo(customRoleId);
        }

        @Test
        @DisplayName("Should throw exception when removing last owner")
        @Rollback
        void shouldThrowExceptionWhenRemovingLastOwner() {
            // Given
            Long ownerMemberId = testProject.getProjectMembers().stream()
                    .filter(member -> member.getUser().getId().equals(ownerUser.getId()))
                    .findFirst().get().getId();
            int baseRoleId = testProject.getBaseRole().getId();

            // When & Then
            assertThatThrownBy(() -> projectRoleService.assignRole(
                    ownerUser.getId(),
                    baseRoleId,
                    ownerMemberId,
                    testProject.getId()
            )).isInstanceOf(InvalidRoleAssignmentException.class)
                    .hasMessageContaining("At least one OWNER must exist");
        }
    }
    @Nested
    @DisplayName("Assign Role Tests")
    class renameRoleTests {

        @Test
        @DisplayName("Should rename Role successfully")
        void shouldRenameRoleSuccessfully() {
            ProjectRole role = testProject.getBaseRole();
            int baseRoleId = role.getId();

            ProjectRoleResponse result = projectRoleService.renameRole(ownerUser.getId(), "NewRoleName", testProject.getId(), baseRoleId);

            assertThat(result).isNotNull();
            assertEquals("NewRoleName", result.getName());
            assertEquals(baseRoleId, result.getId());

            // Verify in database
            Project savedProject = projectRepository.findProjectWithInvitesAndMember(testProject.getId()).get();

            assertEquals(savedProject.getBaseRole().getId(), baseRoleId);
            assertEquals(savedProject.getBaseRole().getName(), result.getName());
        }
    }
}
*/