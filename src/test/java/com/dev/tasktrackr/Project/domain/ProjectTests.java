package com.dev.tasktrackr.Project.domain;

import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleAssignmentException;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleDeletion;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.InvalidMemberRemovalException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.ProjectInviteAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.RoleNameAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.UserAlreadyPartOfProjectException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectMemberNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.RoleNotFoundException;
import com.dev.tasktrackr.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Project Entity Tests")
public class ProjectTests {

    @Mock
    private UserEntity mockUser;
    @Mock
    private UserEntity mockReceiver;

    private Project project;
    private ProjectRequest projectRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock users with IDs
        when(mockUser.getId()).thenReturn("user123");
        when(mockReceiver.getId()).thenReturn("receiver789");

        // Setup project request
        projectRequest = new ProjectRequest("Test Project", ProjectType.BASIC);

        // Create fresh project for each test
        project = Project.create(projectRequest, mockUser);

        // Set manual Ids

        // Member ID
        ProjectMember member = project.getProjectMembers().stream()
                .filter(m -> m.getUser().getId().equals("user123"))
                .findFirst().get();
        ReflectionTestUtils.setField(member, "id", 1L);

        // Role IDs
        ProjectRole ownerRole = project.getOwnerRole();
        ReflectionTestUtils.setField(ownerRole, "id", 1);

        ProjectRole baseRole = project.getBaseRole();
        ReflectionTestUtils.setField(baseRole, "id", 2);
    }

    @Nested
    @DisplayName("Project Creation Tests")
    class ProjectCreationTests {

        @Test
        @DisplayName("Should create project with correct name and type")
        void shouldCreateProjectWithNameAndProjectType() {
            Project createdProject = Project.create(projectRequest, mockUser);

            assertNotNull(createdProject);
            assertEquals("Test Project", createdProject.getName());
            assertEquals(ProjectType.BASIC, createdProject.getProjectType());
        }

        @Test
        @DisplayName("Should initialize base roles on creation")
        void shouldInitializeBaseRolesOnCreation() {
            Project createdProject = Project.create(projectRequest, mockUser);
            ProjectRole ownerRole = createdProject.getOwnerRole();
            ProjectRole baseRole = createdProject.getBaseRole();

            assertNotNull(ownerRole);
            assertNotNull(baseRole);
            assertEquals(RoleType.OWNER, ownerRole.getRoleType());
            assertEquals(RoleType.BASE, baseRole.getRoleType());
            assertEquals(2, createdProject.getProjectRoles().size());
        }

        @Test
        @DisplayName("Should add creator as ProjectMember with Owner Role")
        void shouldAddCreatorAsOwnerMember() {
            Project createdProject = Project.create(projectRequest, mockUser);

            assertEquals(1, createdProject.getProjectMembers().size());

            ProjectMember ownerMember = createdProject.getProjectMembers().iterator().next();
            assertEquals(RoleType.OWNER, ownerMember.getProjectRole().getRoleType());
            assertEquals(mockUser, ownerMember.getUser());
            ;
        }
    }

    @Nested
    @DisplayName("Member Managment Tests")
    class MemberManagementTests {

        @Test
        @DisplayName("Should add new member successfully")
        void shouldAddMemberSuccessfully() {
            UserEntity newUser = mock(UserEntity.class);
            when(newUser.getId()).thenReturn("newUser123");

            ProjectMember addedMember = project.addMember(newUser);

            assertEquals(2, project.getProjectMembers().size());
            assertEquals(RoleType.BASE, addedMember.getProjectRole().getRoleType());
        }

        @Test
        @DisplayName("Should add new Member with Role successfully")
        void shouldAddMemberWithRoleSuccessfully() {
            UserEntity newUser = mock(UserEntity.class);
            when(newUser.getId()).thenReturn("newUser123");
            ProjectRole testRole = ProjectRole.createCustomRole(project, "TestRolle", new HashSet<>());
            ProjectMember addedMember = project.addMemberWithRole(newUser, testRole);

            assertEquals(2, project.getProjectMembers().size());
            assertEquals(RoleType.CUSTOM, addedMember.getProjectRole().getRoleType());
            assertEquals("TestRolle", addedMember.getProjectRole().getName());
        }

        @Test
        @DisplayName("Should throw if member is already part of project")
        void shouldThrowIfMemberIsAlreadyPartOfProject() {
            assertThrows(UserAlreadyPartOfProjectException.class, () -> project.addMember(mockUser));
        }

        @Test
        @DisplayName("Should remove Member successfully")
        void shouldRemoveCorrectMemberSuccessfully() {
            UserEntity regularUser = mock(UserEntity.class);
            when(regularUser.getId()).thenReturn("regularUser123");
            ProjectMember member = project.addMember(regularUser);
            ReflectionTestUtils.setField(member, "id", 2L);

            project.removeMember(member.getId());

            assertEquals(1, project.getProjectMembers().size());
            assertEquals(RoleType.OWNER, project.getProjectMembers().iterator().next().getProjectRole().getRoleType());
        }

        @Test
        @DisplayName("Should throw if member to remove is not part of project")
        void shouldThrowIfMemberIsNotPartOfProject() {
            assertThrows(ProjectMemberNotFoundException.class, () -> project.removeMember(999L));
        }

        @Test
        @DisplayName("Should throw if member to remove has OWNER roleType")
        void shouldThrowExceptionWhenRemovingOwner() {
            assertThrows(InvalidMemberRemovalException.class, () -> project.removeMember(1L));
        }
    }

    @Nested
    @DisplayName("Invite Management Tests")
    class InviteManagementTests {
        @Test
        @DisplayName("Should create invite successfully")
        void shouldCreateInviteSuccessfully() {
            project.createInvite(mockUser, mockReceiver);

            assertEquals(1, project.getProjectInvites().size());
            ProjectInvite invite = project.getProjectInvites().get(0);
            assertEquals(mockUser, invite.getSender());
            assertEquals(mockReceiver, invite.getReceiver());
        }

        @Test
        @DisplayName("Should throw if invite to Receiver already exists")
        void shouldThrowIfInviteAlreadyExists() {
            project.createInvite(mockUser, mockReceiver);
            assertThrows(ProjectInviteAlreadyExistsException.class, () -> project.createInvite(mockUser, mockReceiver));
        }

        @Test
        @DisplayName("Should throw if Receiver is already part of project")
        void shouldThrowIfReceiverIsAlreadyPartOfProject() {
            assertThrows(UserAlreadyPartOfProjectException.class, () -> project.createInvite(mockUser, mockUser));
        }

        @Test
        @DisplayName("Shold throw if Sender is not part of project")
        void shouldThrowIfSenderIsNotPartOfProject() {
            UserEntity nonMember = mock(UserEntity.class);
            assertThrows(UserNotProjectMemberException.class, () -> project.createInvite(mockReceiver, nonMember));
        }
    }

    @Nested
    @DisplayName("Role Management Tests")
    class RoleManagementTests {
        @Test
        @DisplayName("Should create custom role successfully")
        void shouldCreateCustomRoleSuccessfully() {
            Set<PermissionName> permissions = EnumSet.of(
                    PermissionName.BASIC_CREATE_TASK,
                    PermissionName.COMMON_INVITE_USER
            );

            project.createRole("Custom Role", permissions);
            ProjectRole role = project.getProjectRoles().stream()
                    .filter(r -> r.getName().equals("Custom Role"))
                    .findFirst().orElseThrow(() -> new AssertionError("Role not found"));

            assertEquals(3, project.getProjectRoles().size());
            assertEquals(RoleType.CUSTOM, role.getRoleType());
            assertEquals(2, role.getPermissions().size());
        }

        @Test
        @DisplayName("Should throw exception for duplicate role name")
        void shouldThrowExceptionForDuplicateRoleName() {
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);

            assertThrows(RoleNameAlreadyExistsException.class,
                    () -> project.createRole("OWNER", permissions));
        }

        @Test
        @DisplayName("Should delete custom role successfully")
        void shouldDeleteCustomRoleSuccessfully() {
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            project.createRole("Deletable Role", permissions);
            int roleId = project.getProjectRoles().stream()
                    .filter(r -> r.getName().equals("Deletable Role"))
                    .findFirst().get().getId();

            project.deleteRole(roleId);

            assertEquals(2, project.getProjectRoles().size());
            assertFalse(project.getProjectRoles().stream()
                    .anyMatch(r -> r.getName().equals("Deletable Role")));
        }

        @Test
        @DisplayName("Should throw exception when deleting base role")
        void shouldThrowExceptionWhenDeletingBaseRole() {
            int baseRoleId = project.getBaseRole().getId();

            assertThrows(InvalidRoleDeletion.class,
                    () -> project.deleteRole(baseRoleId));
        }

        @Test
        @DisplayName("Should throw exception when deleting owner role")
        void shouldThrowExceptionWhenDeletingOwnerRole() {
            int ownerRoleId = project.getOwnerRole().getId();

            assertThrows(InvalidRoleDeletion.class,
                    () -> project.deleteRole(ownerRoleId));
        }

        @Test
        @DisplayName("Should throw exception when deleting role in use")
        void shouldThrowExceptionWhenDeletingRoleInUse() {
            UserEntity newUser = mock(UserEntity.class);
            when(newUser.getId()).thenReturn("newuser123");
            project.addMember(newUser);

            int baseRoleId = project.getBaseRole().getId();

            assertThrows(InvalidRoleDeletion.class,
                    () -> project.deleteRole(baseRoleId));
        }

        @Test
        @DisplayName("Should rename role successfully")
        void shouldRenameRoleSuccessfully() {
            // Create custom role first
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            project.createRole("Original Name", permissions);

            int roleId = project.getProjectRoles().stream()
                    .filter(r -> r.getName().equals("Original Name"))
                    .findFirst().get().getId();

            ProjectRole renamedRole = project.renameRole(roleId, "New Name");

            assertEquals("New Name", renamedRole.getName());
        }

        @Test
        @DisplayName("Should throw exception when renaming to existing name")
        void shouldThrowExceptionWhenRenamingToExistingName() {
            // Create custom role
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            project.createRole("Custom Role", permissions);

            int roleId = project.getProjectRoles().stream()
                    .filter(r -> r.getName().equals("Custom Role"))
                    .findFirst().get().getId();

            assertThrows(RoleNameAlreadyExistsException.class,
                    () -> project.renameRole(roleId, "OWNER"));
        }

        @Test
        @DisplayName("Should throw when role does not exist")
        void shouldThrowWhenRoleIdDoesNotExists() {
            assertThrows(RoleNotFoundException.class,
                    () -> project.renameRole(999, "NEUEROLLE"));
        }
    }

    @Nested
    @DisplayName("Role Assignment Tests")
    class RoleAssignmentTests {

        @Test
        @DisplayName("Should assign role successfully")
        void shouldAssignRoleSuccessfully() {
            // Add member and create custom role
            UserEntity newUser = mock(UserEntity.class);
            when(newUser.getId()).thenReturn("newuser123");
            ProjectMember newMember = project.addMember(newUser);
            ReflectionTestUtils.setField(newMember, "id", 3L);

            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            project.createRole("Custom Role", permissions);

            Long memberId = project.getProjectMembers().stream()
                    .filter(m -> m.getUser().getId().equals("newuser123"))
                    .findFirst().get().getId();

            int roleId = project.getProjectRoles().stream()
                    .filter(r -> r.getName().equals("Custom Role"))
                    .findFirst().get().getId();

            ProjectMember result = project.assignRole(roleId, memberId, mockUser.getId());

            assertEquals("Custom Role", result.getProjectRole().getName());
        }

        @Test
        @DisplayName("Should throw exception when member not found")
        void shouldThrowExceptionWhenMemberNotFound() {
            int ownerRoleId = project.getOwnerRole().getId();

            assertThrows(ProjectMemberNotFoundException.class,
                    () -> project.assignRole(ownerRoleId, 999L, mockUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception when acting user not member")
        void shouldThrowExceptionWhenActingUserNotMember() {
            Long memberId = project.getProjectMembers().iterator().next().getId();
            int ownerRoleId = project.getOwnerRole().getId();

            assertThrows(UserNotProjectMemberException.class,
                    () -> project.assignRole(ownerRoleId, memberId, "nonmember123"));
        }

        @Test
        @DisplayName("Should throw exception when removing last owner")
        void shouldThrowExceptionWhenRemovingLastOwner() {
            Long ownerMemberId = project.getProjectMembers().stream()
                    .filter(m -> m.getProjectRole().getRoleType() == RoleType.OWNER)
                    .findFirst().get().getId();

            int baseRoleId = project.getBaseRole().getId();

            assertThrows(InvalidRoleAssignmentException.class,
                    () -> project.assignRole(baseRoleId, ownerMemberId, mockUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception when non-owner tries to assign owner role to self")
        void shouldThrowExceptionWhenNonOwnerTriesToAssignOwnerRoleToSelf() {
            // Add regular member
            UserEntity regularUser = mock(UserEntity.class);
            when(regularUser.getId()).thenReturn("regular123");
            ProjectMember regularMember = project.addMember(regularUser);
            ReflectionTestUtils.setField(regularMember, "id", 3L);

            int ownerRoleId = project.getOwnerRole().getId();

            assertThrows(InvalidRoleAssignmentException.class,
                    () -> project.assignRole(ownerRoleId, regularMember.getId(), "regular123"));
        }
    }

    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {
        @Test
        @DisplayName("Should get owner role successfully")
        void shouldGetOwnerRoleSuccessfully() {
            ProjectRole ownerRole = project.getOwnerRole();

            assertNotNull(ownerRole);
            assertEquals(RoleType.OWNER, ownerRole.getRoleType());
            assertEquals("OWNER", ownerRole.getName());
        }

        @Test
        @DisplayName("Should get base role successfully")
        void shouldGetBaseRoleSuccessfully() {
            ProjectRole baseRole = project.getBaseRole();

            assertNotNull(baseRole);
            assertEquals(RoleType.BASE, baseRole.getRoleType());
            assertEquals("BASE", baseRole.getName());
        }

        @Test
        @DisplayName("Should find ProjectMember successfully")
        void shouldFindProjectMemberSuccessfully() {

            ProjectMember projectMember = project.findProjectMember("user123");

            assertNotNull(projectMember);
            assertEquals("user123", projectMember.getUser().getId());
        }

        @Test
        @DisplayName("Should throw if ProjectMember is not found")
        void shouldThrowIfProjectMemberIsNotFound() {
            assertThrows(UserNotProjectMemberException.class, () -> project.findProjectMember("invalidUserId123"));
        }
    }
}
