package com.dev.tasktrackr.ProjectTests.domain;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleAssignmentException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.InvalidMemberRemovalException;
import com.dev.tasktrackr.user.UserEntity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
    @DisplayName("State Validation Tests")
    class StateValidationTests {

        @Test
        @DisplayName("Should throw if member has OWNER roleType and is removed")
        void shouldThrowWhenOwnerIsRemoved() {
            when(mockRole.getRoleType()).thenReturn(RoleType.OWNER);

            assertThrows(InvalidMemberRemovalException.class, () -> projectMember.canBeRemovedFromProject());
        }

        @Test
        @DisplayName("Should not throw if member is not OWNER and is removed")
        void shouldNotThrowWhenNonOwnerIsRemoved() {
            when(mockRole.getRoleType()).thenReturn(RoleType.BASE);

            assertDoesNotThrow(() -> projectMember.canBeRemovedFromProject());
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {

        // Liefert alle (Permission, zugehörige Methode) Paare
        private static Stream<Arguments> permissionChecksProvider() {
            return Stream.of(
                    Arguments.of(PermissionName.BASIC_CREATE_TASK, (Consumer<ProjectMember>) ProjectMember::canCreateTask),
                    Arguments.of(PermissionName.BASIC_DELETE_TASK, (Consumer<ProjectMember>) ProjectMember::canDeleteTask),
                    Arguments.of(PermissionName.BASIC_EDIT_INFORMATION, (Consumer<ProjectMember>) ProjectMember::canEditInformation),
                    Arguments.of(PermissionName.COMMON_INVITE_USER, (Consumer<ProjectMember>) ProjectMember::canInviteUser),
                    Arguments.of(PermissionName.COMMON_REMOVE_USER, (Consumer<ProjectMember>) ProjectMember::canRemoveUser),
                    Arguments.of(PermissionName.COMMON_MANAGE_ROLES, (Consumer<ProjectMember>) ProjectMember::canManageRoles),
                    Arguments.of(PermissionName.SCRUM_CREATE_USER_STORY, (Consumer<ProjectMember>) ProjectMember::canCreateUserStory),
                    Arguments.of(PermissionName.SCRUM_DELETE_USER_STORY, (Consumer<ProjectMember>) ProjectMember::canDeleteUserStory),
                    Arguments.of(PermissionName.SCRUM_PLAN_SPRINT, (Consumer<ProjectMember>) ProjectMember::canPlanSprint),
                    Arguments.of(PermissionName.SCRUM_START_SPRINT, (Consumer<ProjectMember>) ProjectMember::canStartSprint),
                    Arguments.of(PermissionName.SCRUM_END_SPRINT, (Consumer<ProjectMember>) ProjectMember::canEndSprint),
                    Arguments.of(PermissionName.SCRUM_ASSIGN_USER_TO_STORY, (Consumer<ProjectMember>) ProjectMember::canAssignUserToStory),
                    Arguments.of(PermissionName.SCRUM_CAN_DELETE_COMMENTS_AND_BLOCKER, (Consumer<ProjectMember>) ProjectMember::canDeleteCommentsAndBlocker)
            );
        }

        @ParameterizedTest
        @MethodSource("permissionChecksProvider")
        @DisplayName("Should not throw if role has required permission")
        void shouldNotThrowIfRoleHasPermission(PermissionName permission, Consumer<ProjectMember> methodToCall) {
            when(mockRole.hasPermission(permission)).thenReturn(true);

            assertDoesNotThrow(() -> methodToCall.accept(projectMember));
        }

        @ParameterizedTest
        @MethodSource("permissionChecksProvider")
        @DisplayName("Should throw if role does not have required permission")
        void shouldThrowIfRoleDoesNotHavePermission(PermissionName permission, Consumer<ProjectMember> methodToCall) {
            when(mockRole.hasPermission(permission)).thenReturn(false);

            assertThrows(PermissionDeniedException.class, () -> methodToCall.accept(projectMember));
        }

        // Separater Test für die boolean-Methode
        @Test
        @DisplayName("Should return true for canUpdateStoryStatus if permission exists")
        void shouldReturnTrueForUpdateStoryStatusWhenPermissionExists() {
            when(mockRole.hasPermission(PermissionName.SCRUM_UPDATE_STORY_STATUS)).thenReturn(true);

            assertTrue(projectMember.canUpdateStoryStatus());
        }

        @Test
        @DisplayName("Should return false for canUpdateStoryStatus if permission is missing")
        void shouldReturnFalseForUpdateStoryStatusWhenPermissionIsMissing() {
            when(mockRole.hasPermission(PermissionName.SCRUM_UPDATE_STORY_STATUS)).thenReturn(false);

            assertFalse(projectMember.canUpdateStoryStatus());
        }
    }
}