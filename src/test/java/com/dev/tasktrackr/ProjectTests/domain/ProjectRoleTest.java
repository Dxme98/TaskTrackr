package com.dev.tasktrackr.ProjectTests.domain;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleAssignmentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
@DisplayName("ProjectRole Entity Tests")
public class ProjectRoleTest {

    @Mock
    private Project mockProject;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockProject.getProjectType()).thenReturn(ProjectType.BASIC);
    }

    @Nested
    @DisplayName("Custom Role Creation Tests")
    class CustomRoleCreationTests {

        @Test
        @DisplayName("Should create custom role with valid BASIC permissions")
        void shouldCreateCustomRoleWithValidBasicPermissions() {
            Set<PermissionName> permissions = EnumSet.of(
                    PermissionName.BASIC_CREATE_TASK,
                    PermissionName.COMMON_INVITE_USER
            );

            ProjectRole role = ProjectRole.createCustomRole(mockProject, "Developer", permissions);

            assertEquals("Developer", role.getName());
            assertEquals(RoleType.CUSTOM, role.getRoleType());
            assertEquals(mockProject, role.getProject());
            assertTrue(role.hasPermission(PermissionName.BASIC_CREATE_TASK));
            assertTrue(role.hasPermission(PermissionName.COMMON_INVITE_USER));
        }

        @Test
        @DisplayName("Should create custom role with only COMMON permissions")
        void shouldCreateCustomRoleWithOnlyCommonPermissions() {
            Set<PermissionName> permissions = EnumSet.of(
                    PermissionName.COMMON_INVITE_USER,
                    PermissionName.COMMON_REMOVE_USER
            );

            ProjectRole role = ProjectRole.createCustomRole(mockProject, "Moderator", permissions);

            assertEquals("Moderator", role.getName());
            assertTrue(role.hasPermission(PermissionName.COMMON_INVITE_USER));
            assertTrue(role.hasPermission(PermissionName.COMMON_REMOVE_USER));
        }

        @Test
        @DisplayName("Should throw exception for invalid permissions for BASIC project")
        void shouldThrowExceptionForInvalidPermissionsForBasicProject() {
            Set<PermissionName> invalidPermissions = EnumSet.of(
                    PermissionName.BASIC_CREATE_TASK,
                    PermissionName.SCRUM_CREATE_USER_STORY // Invalid for BASIC project
            );

            assertThrows(InvalidRoleAssignmentException.class,
                    () -> ProjectRole.createCustomRole(mockProject, "Invalid Role", invalidPermissions));
        }

        @Test
        @DisplayName("Should throw exception for SCRUM permissions in BASIC project")
        void shouldThrowExceptionForScrumPermissionsInBasicProject() {
            when(mockProject.getProjectType()).thenReturn(ProjectType.BASIC);

            Set<PermissionName> scrumPermissions = EnumSet.of(
                    PermissionName.SCRUM_CREATE_USER_STORY
            );

            assertThrows(InvalidRoleAssignmentException.class,
                    () -> ProjectRole.createCustomRole(mockProject, "Scrum Master", scrumPermissions));
        }
    }

    @Nested
    @DisplayName("Owner Role Creation Tests")
    class OwnerRoleCreationTests {
        @Test
        @DisplayName("Should create BASIC owner role with correct permissions")
        void shouldCreateBasicOwnerRoleWithCorrectPermissions() {
            when(mockProject.getProjectType()).thenReturn(ProjectType.BASIC);

            ProjectRole ownerRole = ProjectRole.createOwnerRole(mockProject, ProjectType.BASIC);

            assertEquals("OWNER", ownerRole.getName());
            assertEquals(RoleType.OWNER, ownerRole.getRoleType());
            assertEquals(mockProject, ownerRole.getProject());

            // Should have BASIC permissions
            assertTrue(ownerRole.hasPermission(PermissionName.BASIC_CREATE_TASK));
            assertTrue(ownerRole.hasPermission(PermissionName.BASIC_DELETE_TASK));
            assertTrue(ownerRole.hasPermission(PermissionName.BASIC_EDIT_INFORMATION));

            // Should have COMMON permissions
            assertTrue(ownerRole.hasPermission(PermissionName.COMMON_INVITE_USER));
            assertTrue(ownerRole.hasPermission(PermissionName.COMMON_REMOVE_USER));
            assertTrue(ownerRole.hasPermission(PermissionName.COMMON_MANAGE_ROLES));
        }

        @Test
        @DisplayName("Should create SCRUM owner role with correct permissions")
        void shouldCreateScrumOwnerRoleWithCorrectPermissions() {
            when(mockProject.getProjectType()).thenReturn(ProjectType.SCRUM);

            ProjectRole ownerRole = ProjectRole.createOwnerRole(mockProject, ProjectType.SCRUM);

            assertEquals("OWNER", ownerRole.getName());
            assertEquals(RoleType.OWNER, ownerRole.getRoleType());

            // Should have COMMON permissions
            assertTrue(ownerRole.hasPermission(PermissionName.COMMON_INVITE_USER));
            assertTrue(ownerRole.hasPermission(PermissionName.COMMON_REMOVE_USER));
            assertTrue(ownerRole.hasPermission(PermissionName.COMMON_MANAGE_ROLES));

            // TODO: Add SCRUM permission assertions when getScrumPermissions() is implemented
        }
    }

    @Nested
    @DisplayName("Base Role Creation Tests")
    class BaseRoleCreationTests {

        @Test
        @DisplayName("Should create base role with minimal permissions")
        void shouldCreateBaseRoleWithMinimalPermissions() {
            ProjectRole baseRole = ProjectRole.createBaseRole(mockProject);

            assertEquals("BASE", baseRole.getName());
            assertEquals(RoleType.BASE, baseRole.getRoleType());
            assertEquals(mockProject, baseRole.getProject());
            assertTrue(baseRole.getPermissions().isEmpty());
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {
        private ProjectRole roleWithPermissions;

        @BeforeEach
        void setUp() {
            Set<PermissionName> permissions = EnumSet.of(
                    PermissionName.BASIC_CREATE_TASK,
                    PermissionName.COMMON_INVITE_USER
            );
            roleWithPermissions = ProjectRole.createCustomRole(mockProject, "Test Role", permissions);
        }

        @Test
        @DisplayName("Should return true for existing permission")
        void shouldReturnTrueForExistingPermission() {
            assertTrue(roleWithPermissions.hasPermission(PermissionName.BASIC_CREATE_TASK));
            assertTrue(roleWithPermissions.hasPermission(PermissionName.COMMON_INVITE_USER));
        }

        @Test
        @DisplayName("Should return false for non-existing permission")
        void shouldReturnFalseForNonExistingPermission() {
            assertFalse(roleWithPermissions.hasPermission(PermissionName.BASIC_DELETE_TASK));
            assertFalse(roleWithPermissions.hasPermission(PermissionName.COMMON_REMOVE_USER));
        }
    }

    @Nested
    @DisplayName("Role Renaming Tests")
    class RoleRenamingTests {

        private ProjectRole customRole;

        @BeforeEach
        void setUp() {
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            customRole = ProjectRole.createCustomRole(mockProject, "Original Name", permissions);
        }

        @Test
        @DisplayName("Should rename role successfully")
        void shouldRenameRoleSuccessfully() {
            ProjectRole renamedRole = customRole.renameRole("New Name");

            assertEquals("New Name", renamedRole.getName());
            assertSame(customRole, renamedRole); // Should return same instance
        }

        @Test
        @DisplayName("Should handle empty name")
        void shouldHandleEmptyName() {
            ProjectRole renamedRole = customRole.renameRole("");

            assertEquals("", renamedRole.getName());
        }

        @Test
        @DisplayName("Should handle null name")
        void shouldHandleNullName() {
            ProjectRole renamedRole = customRole.renameRole(null);

            assertNull(renamedRole.getName());
        }
    }

    @Nested
    @DisplayName("Permission Validation Tests")
    class PermissionValidationTests {

        @Test
        @DisplayName("Should allow BASIC permissions for BASIC project")
        void shouldAllowBasicPermissionsForBasicProject() {
            when(mockProject.getProjectType()).thenReturn(ProjectType.BASIC);

            Set<PermissionName> basicPermissions = EnumSet.of(
                    PermissionName.BASIC_CREATE_TASK,
                    PermissionName.BASIC_DELETE_TASK,
                    PermissionName.BASIC_EDIT_INFORMATION
            );

            assertDoesNotThrow(() ->
                    ProjectRole.createCustomRole(mockProject, "Basic Role", basicPermissions));
        }

        @Test
        @DisplayName("Should allow COMMON permissions for any project type")
        void shouldAllowCommonPermissionsForAnyProjectType() {
            Set<PermissionName> commonPermissions = EnumSet.of(
                    PermissionName.COMMON_INVITE_USER,
                    PermissionName.COMMON_REMOVE_USER,
                    PermissionName.COMMON_MANAGE_ROLES
            );

            // Test for BASIC project
            when(mockProject.getProjectType()).thenReturn(ProjectType.BASIC);
            assertDoesNotThrow(() ->
                    ProjectRole.createCustomRole(mockProject, "Common Role Basic", commonPermissions));

            // Test for SCRUM project
            when(mockProject.getProjectType()).thenReturn(ProjectType.SCRUM);
            assertDoesNotThrow(() ->
                    ProjectRole.createCustomRole(mockProject, "Common Role Scrum", commonPermissions));
        }

        @Test
        @DisplayName("Should reject SCRUM permissions for BASIC project")
        void shouldRejectScrumPermissionsForBasicProject() {
            when(mockProject.getProjectType()).thenReturn(ProjectType.BASIC);

            Set<PermissionName> scrumPermissions = EnumSet.of(
                    PermissionName.SCRUM_CREATE_USER_STORY
            );

            InvalidRoleAssignmentException exception = assertThrows(
                    InvalidRoleAssignmentException.class,
                    () -> ProjectRole.createCustomRole(mockProject, "Invalid Role", scrumPermissions)
            );

            assertTrue(exception.getMessage().contains("SCRUM_CREATE_USER_STORY"));
            assertTrue(exception.getMessage().contains("BASIC"));
        }

        @Test
        @DisplayName("Should reject BASIC permissions for SCRUM project")
        void shouldRejectBasicPermissionsForScrumProject() {
            when(mockProject.getProjectType()).thenReturn(ProjectType.SCRUM);

            Set<PermissionName> basicPermissions = EnumSet.of(
                    PermissionName.BASIC_CREATE_TASK
            );

            assertThrows(InvalidRoleAssignmentException.class,
                    () -> ProjectRole.createCustomRole(mockProject, "Invalid Role", basicPermissions));
        }

        @Test
        @DisplayName("Should allow mixed COMMON and project-specific permissions")
        void shouldAllowMixedCommonAndProjectSpecificPermissions() {
            when(mockProject.getProjectType()).thenReturn(ProjectType.BASIC);

            Set<PermissionName> mixedPermissions = EnumSet.of(
                    PermissionName.BASIC_CREATE_TASK,
                    PermissionName.COMMON_INVITE_USER,
                    PermissionName.BASIC_EDIT_INFORMATION,
                    PermissionName.COMMON_MANAGE_ROLES
            );

            assertDoesNotThrow(() ->
                    ProjectRole.createCustomRole(mockProject, "Mixed Role", mixedPermissions));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should create role with empty permission set")
        void shouldCreateRoleWithEmptyPermissionSet() {
            Set<PermissionName> emptyPermissions = EnumSet.noneOf(PermissionName.class);

            ProjectRole role = ProjectRole.createCustomRole(mockProject, "Empty Role", emptyPermissions);

            assertEquals("Empty Role", role.getName());
            assertEquals(RoleType.CUSTOM, role.getRoleType());
            assertFalse(role.hasPermission(PermissionName.BASIC_CREATE_TASK));
        }

        @Test
        @DisplayName("Should handle role name with special characters")
        void shouldHandleRoleNameWithSpecialCharacters() {
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);

            ProjectRole role = ProjectRole.createCustomRole(mockProject, "Role-With_Special@Characters!", permissions);

            assertEquals("Role-With_Special@Characters!", role.getName());
        }

        @Test
        @DisplayName("Should handle very long role name")
        void shouldHandleVeryLongRoleName() {
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            String longName = "Very".repeat(50) + "LongRoleName";

            ProjectRole role = ProjectRole.createCustomRole(mockProject, longName, permissions);

            assertEquals(longName, role.getName());
        }
    }
}
*/