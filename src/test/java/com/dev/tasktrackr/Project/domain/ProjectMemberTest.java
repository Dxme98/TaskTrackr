package com.dev.tasktrackr.Project.domain;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleAssignmentException;
import com.dev.tasktrackr.user.UserEntity;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ProjectMember Entity Tests")
public class ProjectMemberTest {
    @Mock
    private UserEntity mockUser;
    @Mock
    private Project mockProject;
    @Mock
    private ProjectRole mockRole;
    @Mock
    private ProjectRole mockNewRole;

    private ProjectMember projectMember;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(mockUser.getId()).thenReturn("user123");
        when(mockProject.getId()).thenReturn(1L);
        when(mockRole.getProject()).thenReturn(mockProject);
        when(mockNewRole.getProject()).thenReturn(mockProject);
        when(mockRole.getPermissions()).thenReturn(new HashSet<>());

        projectMember = new ProjectMember(mockUser, mockProject, mockRole);
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create member with constructor")
        void shouldCreateMemberWithConstructor() {
            assertEquals(mockUser, projectMember.getUser());
            assertEquals(mockProject, projectMember.getProject());
            assertEquals(mockRole, projectMember.getProjectRole());
        }

        @Test
        @DisplayName("Should create member with static factory method")
        void shouldCreateMemberWithStaticFactoryMethod() {
            ProjectMember member = ProjectMember.createMember(mockUser, mockProject, mockRole);

            assertEquals(mockUser, member.getUser());
            assertEquals(mockProject, member.getProject());
            assertEquals(mockRole, member.getProjectRole());
        }
    }

    @Nested
    @DisplayName("Role Assignment Tests")
    class RoleAssignmentTests {

        @Test
        @DisplayName("Should assign role successfully")
        void shouldAssignRoleSuccessfully() {
            projectMember.assignRole(mockNewRole);

            assertEquals(mockNewRole, projectMember.getProjectRole());
        }

        @Test
        @DisplayName("Should throw exception when role belongs to different project")
        void shouldThrowIfRoleIsNotPartOfProject() {
            ProjectRole notPartOfProjectRole = mock(ProjectRole.class);
            Project differentProject = mock(Project.class);
            when(notPartOfProjectRole.getProject()).thenReturn(differentProject);

            assertThrows(InvalidRoleAssignmentException.class, () -> projectMember.assignRole(notPartOfProjectRole));
        }

        @Test
        @DisplayName("Should not change role when exception is thrown")
        void shouldNotChangeRoleWhenExceptionIsThrown() {
            Project differentProject = mock(Project.class);
            when(mockNewRole.getProject()).thenReturn(differentProject);

            ProjectRole originalRole = projectMember.getProjectRole();

            assertThrows(InvalidRoleAssignmentException.class,
                    () -> projectMember.assignRole(mockNewRole));

            assertEquals(originalRole, projectMember.getProjectRole());
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {

        @Test
        void shouldNotThrowIfRoleHaveCreateTaskPermission() {
            when(mockNewRole.hasPermission(PermissionName.BASIC_CREATE_TASK)).thenReturn(true);
            projectMember.assignRole(mockNewRole);

            assertDoesNotThrow(() -> projectMember.canCreateTask());
        }

        @Test
        void shouldNotThrowIfRoleHasDeleteTaskPermission() {
            when(mockNewRole.hasPermission(PermissionName.BASIC_DELETE_TASK)).thenReturn(true);
            projectMember.assignRole(mockNewRole);

            assertDoesNotThrow(() -> projectMember.canDeleteTask());
        }

        @Test
        void shouldNotThrowIfRoleHasEditInformationPermission() {
            when(mockNewRole.hasPermission(PermissionName.BASIC_EDIT_INFORMATION)).thenReturn(true);
            projectMember.assignRole(mockNewRole);

            assertDoesNotThrow(() -> projectMember.canEditInformation());
        }

        @Test
        void shouldNotThrowIfRoleHasInviteUserPermission() {
            when(mockNewRole.hasPermission(PermissionName.COMMON_INVITE_USER)).thenReturn(true);
            projectMember.assignRole(mockNewRole);

            assertDoesNotThrow(() -> projectMember.canInviteUser());
        }

        @Test
        void shouldNotThrowIfRoleHasRemoveUserPermission() {
            when(mockNewRole.hasPermission(PermissionName.COMMON_REMOVE_USER)).thenReturn(true);
            projectMember.assignRole(mockNewRole);

            assertDoesNotThrow(() -> projectMember.canRemoveUser());
        }

        @Test
        void shouldNotThrowIfRoleHasManageRolesPermission() {
            when(mockNewRole.hasPermission(PermissionName.COMMON_MANAGE_ROLES)).thenReturn(true);
            projectMember.assignRole(mockNewRole);

            assertDoesNotThrow(() -> projectMember.canManageRoles());
        }



        @Test
        @DisplayName("Should throw if Role does not have permission")
        void shouldThrowIfRoleDoesNotHaveCreateTaskPermission() {
            assertThrows(PermissionDeniedException.class, () -> projectMember.canCreateTask());
        }

        @Test
        @DisplayName("Should throw if Role does not have permission")
        void shouldThrowIfRoleDoesNotHaveDeleteTaskPermission() {
            assertThrows(PermissionDeniedException.class, () -> projectMember.canDeleteTask());
        }

        @Test
        @DisplayName("Should throw if Role does not have permission")
        void shouldThrowIfRoleDoesNotHaveEditInformationPermission() {
            assertThrows(PermissionDeniedException.class, () -> projectMember.canEditInformation());
        }

        @Test
        @DisplayName("Should throw if Role does not have permission")
        void shouldThrowIfRoleDoesNotHaveInviteUserPermission() {
            assertThrows(PermissionDeniedException.class, () -> projectMember.canInviteUser());
        }

        @Test
        @DisplayName("Should throw if Role does not have permission")
        void shouldThrowIfRoleDoesNotHaveRemoveUserPermission() {
            assertThrows(PermissionDeniedException.class, () -> projectMember.canRemoveUser());
        }

        @Test
        @DisplayName("Should throw if Role does not have permission")
        void shouldThrowIfRoleDoesNotHaveManageRolesPermission() {
            assertThrows(PermissionDeniedException.class, () -> projectMember.canManageRoles());
        }
    }
}
