package com.dev.tasktrackr.ProjectTests.service.ProjectManagement;

import com.dev.tasktrackr.ProjectTests.service.shared.ProjectManagementBaseTest;
import com.dev.tasktrackr.project.api.dtos.request.CreateProjectRoleRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import com.dev.tasktrackr.project.domain.enums.PermissionName;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.repository.ProjectMemberRepository;
import com.dev.tasktrackr.project.repository.ProjectRoleRepository;
import com.dev.tasktrackr.project.service.ProjectRoleService;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleAssignmentException;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidRoleDeletion;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.RoleNameAlreadyExistsException;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ProjectRoleService Integration Tests")
public class ProjectRoleServiceIntegrationTests extends ProjectManagementBaseTest {

    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private ProjectRoleRepository projectRoleRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    private UserEntity ownerUser;
    private UserEntity baseUser;
    private Project testProject;
    private ProjectMember ownerMember;
    private ProjectMember baseMember;

    @BeforeEach
    void setUp() {
        ownerUser = testDataFactory.createTestUser("owner123", "owner");
        baseUser = testDataFactory.createTestUser("base456", "base");

        testProject = testDataFactory.createTestProject("Test Project", ProjectType.BASIC, ownerUser);

        baseMember = testDataFactory.createTestMember(testProject, baseUser);

        ownerMember = projectMemberRepository.findProjectMemberByUserIdAndProjectId(ownerUser.getId(), testProject.getId())
                .orElseThrow(() -> new IllegalStateException("Owner-Mitglied wurde nicht korrekt erstellt."));
    }

    @Nested
    @DisplayName("Create Project Role Tests")
    class CreateProjectRoleTests {

        private CreateProjectRoleRequest createRoleRequest(String name, Set<PermissionName> permissions) {
            CreateProjectRoleRequest request = new CreateProjectRoleRequest();
            request.setName(name);
            request.setPermissions(permissions);
            return request;
        }

        @Test
        @DisplayName("Should create custom role successfully")
        void shouldCreateCustomRoleSuccessfully() {
            // Given
            Set<PermissionName> permissions = EnumSet.of(
                    PermissionName.BASIC_CREATE_TASK,
                    PermissionName.COMMON_INVITE_USER
            );
            CreateProjectRoleRequest request = createRoleRequest("Developer", permissions);
            long initialRoleCount = projectRoleRepository.countByProjectId(testProject.getId()); // 2 (OWNER, BASE)

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
            assertThat(projectRoleRepository.countByProjectId(testProject.getId())).isEqualTo(initialRoleCount + 1);
        }

        @Test
        @DisplayName("Should throw exception when User without permission tries to create role")
        void shouldThrowExceptionWhenUserWithoutPermissionTriesToCreateRole() {
            // Given
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            CreateProjectRoleRequest request = createRoleRequest("Unauthorized Role", permissions);

            // When & Then
            // baseMember hat die BASE-Rolle ohne COMMON_MANAGE_ROLES-Rechte
            assertThatThrownBy(() -> projectRoleService.createProjectRole(
                    baseMember.getUser().getId(),
                    request,
                    testProject.getId()
            )).isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        @DisplayName("Should throw exception for duplicate role name")
        void shouldThrowExceptionForDuplicateRoleName() {
            // Given
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            CreateProjectRoleRequest request = createRoleRequest("OWNER", permissions); // "OWNER" existiert bereits

            // When & Then
            assertThatThrownBy(() -> projectRoleService.createProjectRole(
                    ownerUser.getId(),
                    request,
                    testProject.getId()
            )).isInstanceOf(RoleNameAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("Delete Project Role Tests")
    class DeleteProjectRoleTests {

        private int customRoleId;

        @BeforeEach
        void setUpCustomRole() {
            // Erstelle eine benutzerdefinierte Rolle für die Löschtests
            Set<PermissionName> permissions = EnumSet.of(PermissionName.BASIC_CREATE_TASK);
            CreateProjectRoleRequest request = new CreateProjectRoleRequest();
            request.setName("Deletable Role");
            request.setPermissions(permissions);

            ProjectRoleResponse createdRole = projectRoleService.createProjectRole(
                    ownerUser.getId(),
                    request,
                    testProject.getId()
            );
            customRoleId = createdRole.getId();
        }

        @Test
        @DisplayName("Should delete custom role successfully")
        void shouldDeleteCustomRoleSuccessfully() {
            // Given
            long initialRoleCount = projectRoleRepository.count(); // 3

            // When
            assertDoesNotThrow(() -> projectRoleService.deleteProjectRole(
                    ownerUser.getId(),
                    testProject.getId(),
                    customRoleId
            ));

            // Then
            assertThat(projectRoleRepository.count()).isEqualTo(initialRoleCount - 1);
            assertThat(projectRoleRepository.findById(customRoleId)).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when deleting OWNER role")
        void shouldThrowExceptionWhenDeletingOwnerRole() {
            // Given
            int ownerRoleId = testProject.getOwnerRole().getId();

            // When & Then
            // Testet `roleToDelete.canBeDeleted()`
            assertThatThrownBy(() -> projectRoleService.deleteProjectRole(
                    ownerUser.getId(),
                    testProject.getId(),
                    ownerRoleId
            )).isInstanceOf(InvalidRoleDeletion.class);
        }

        @Test
        @DisplayName("Should throw exception when deleting BASE role")
        void shouldThrowExceptionWhenDeletingBaseRole() {
            // Given
            int baseRoleId = testProject.getBaseRole().getId();

            // When & Then
            // Testet `roleToDelete.canBeDeleted()`
            assertThatThrownBy(() -> projectRoleService.deleteProjectRole(
                    ownerUser.getId(),
                    testProject.getId(),
                    baseRoleId
            )).isInstanceOf(InvalidRoleDeletion.class);
        }
    }

    @Nested
    @DisplayName("Assign Role Tests")
    class AssignRoleTests {

        private int customRoleId;

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
        }

        @Test
        @DisplayName("Should assign role successfully")
        void shouldAssignRoleSuccessfully() {
            // When
            ProjectMemberDto result = projectRoleService.assignRole(
                    ownerUser.getId(),
                    customRoleId,
                    baseMember.getId(), // Weist dem 'baseUser' die neue Rolle zu
                    testProject.getId()
            );
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRole()).isEqualTo("Assignable Role");

            // Verify in database
            ProjectMember updatedMember = projectMemberRepository.findById(baseMember.getId()).get();
            assertThat(updatedMember.getProjectRole().getId()).isEqualTo(customRoleId);
        }

        @Test
        @DisplayName("Should throw exception when removing last owner")
        void shouldThrowExceptionWhenRemovingLastOwner() {
            // Given
            int baseRoleId = testProject.getBaseRole().getId();

            // When & Then
            // Versucht, den 'ownerMember' (den letzten Owner) zur 'BASE'-Rolle herabzustufen
            assertThatThrownBy(() -> projectRoleService.assignRole(
                    ownerUser.getId(),
                    baseRoleId,
                    ownerMember.getId(),
                    testProject.getId()
            )).isInstanceOf(InvalidRoleAssignmentException.class)
                    .hasMessageContaining("At least one OWNER must exist");
        }
    }

    @Nested
    @DisplayName("Rename Role Tests")
    class RenameRoleTests {

        @Test
        @DisplayName("Should rename Role successfully")
        void shouldRenameRoleSuccessfully() {
            // Given
            int baseRoleId = testProject.getBaseRole().getId();
            String newName = "New Base Name";

            // When
            ProjectRoleResponse result = projectRoleService.renameRole(
                    ownerUser.getId(),
                    newName,
                    testProject.getId(),
                    baseRoleId
            );

            // Then
            assertThat(result).isNotNull();
            assertEquals(newName, result.getName());
            assertEquals(baseRoleId, result.getId());

            // Verify in database
            ProjectRole updatedRole = projectRoleRepository.findById(baseRoleId).get();
            assertEquals(newName, updatedRole.getName());
        }

        @Test
        @DisplayName("Should throw exception for duplicate role name on rename")
        void shouldThrowExceptionForDuplicateRoleNameOnRename() {
            // Given
            int baseRoleId = testProject.getBaseRole().getId();

            // When & Then
            assertThatThrownBy(() -> projectRoleService.renameRole(
                    ownerUser.getId(),
                    "OWNER", // Dieser Name existiert bereits
                    testProject.getId(),
                    baseRoleId
            )).isInstanceOf(RoleNameAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("Get All Roles Tests")
    class GetAllRolesTests {

        @Test
        @DisplayName("Should get all roles for project member")
        void shouldGetAllRolesForProjectMember() {
            // Given
            Pageable pageable = Pageable.unpaged();

            // When
            Page<ProjectRoleResponse> result = projectRoleService.getAllRoles(
                    baseUser.getId(), // 'baseUser' ist Mitglied
                    pageable,
                    testProject.getId()
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2); // OWNER, BASE
            assertThat(result.getContent())
                    .extracting(ProjectRoleResponse::getName)
                    .containsExactlyInAnyOrder("OWNER", "BASE");
        }

    }
}
