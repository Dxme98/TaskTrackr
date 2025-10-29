package com.dev.tasktrackr.ProjectTests.service.ProjectManagement;

import com.dev.tasktrackr.ProjectTests.service.shared.ProjectManagementBaseTest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.repository.ProjectInviteRepository;
import com.dev.tasktrackr.project.repository.ProjectMemberRepository;
import com.dev.tasktrackr.project.service.ProjectMemberService;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.InvalidMemberRemovalException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectMemberNotFoundException;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@DisplayName("ProjectMemberService Integration Tests")
public class ProjectMemberServiceIntegrationTest extends ProjectManagementBaseTest {

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectInviteRepository projectInviteRepository;

    // Testdaten-Instanzen
    private UserEntity ownerUser;
    private UserEntity regularUser1;
    private UserEntity regularUser2;
    private UserEntity nonMemberUser;
    private Project testProject;
    private ProjectMember ownerMember;
    private ProjectMember regularMember1;
    private ProjectMember regularMember2;

    @BeforeEach
    void setUp() {
        ownerUser = testDataFactory.createTestUser("owner123", "owner");
        regularUser1 = testDataFactory.createTestUser("regular456", "regular1");
        regularUser2 = testDataFactory.createTestUser("regular789", "regular2");
        nonMemberUser = testDataFactory.createTestUser("nonmember999", "nonmember");

        testProject = testDataFactory.createTestProject("Test Project", ProjectType.BASIC, ownerUser);

        ownerMember = projectMemberRepository.findProjectMemberByUserIdAndProjectId(ownerUser.getId(), testProject.getId())
                .orElseThrow(() -> new IllegalStateException("Owner-Mitglied wurde nicht korrekt erstellt."));

        testDataFactory.createTestInvite(testProject, ownerUser, regularUser1);
        regularMember1 = testDataFactory.createTestMember(testProject, regularUser1);

        testDataFactory.createTestInvite(testProject, ownerUser, regularUser2);
        regularMember2 = testDataFactory.createTestMember(testProject, regularUser2);
    }

    @Nested
    @DisplayName("Get All Project Members Tests")
    class GetAllProjectMembersTests {

        @Test
        @DisplayName("Should get all project members with pagination")
        void shouldGetAllProjectMembersWithPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            long expectedMemberCount = 3; // owner + 2 regular

            // When
            Page<ProjectMemberDto> result = projectMemberService.getAllProjectMembers(
                    ownerUser.getId(),
                    testProject.getId(),
                    pageable
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(expectedMemberCount);
            assertThat(result.getContent()).hasSize((int) expectedMemberCount);
            assertThat(result.getContent())
                    .extracting(ProjectMemberDto::getUsername)
                    .containsExactlyInAnyOrder(
                            ownerUser.getUsername(),
                            regularUser1.getUsername(),
                            regularUser2.getUsername()
                    );
        }

        @Test
        @DisplayName("Should throw exception if user is not project member")
        void shouldThrowExceptionIfUserIsNotProjectMember() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When & Then
            assertThatThrownBy(() -> projectMemberService.getAllProjectMembers(
                    nonMemberUser.getId(), // Nicht-Mitglied
                    testProject.getId(),
                    pageable
            )).isInstanceOf(UserNotProjectMemberException.class);
        }
    }

    @Nested
    @DisplayName("Remove Member Tests")
    class RemoveMemberTests {

        @Test
        @DisplayName("Should remove regular member successfully by owner")
        void shouldRemoveRegularMemberSuccessfully() {
            // Given
            long initialInviteCount = projectInviteRepository.count(); // 2 Invites
            long initialMemberCount = projectMemberRepository.count(); // 3 Members

            // When
            assertDoesNotThrow(() -> projectMemberService.removeMemberFromProject(
                    ownerUser.getId(),
                    testProject.getId(),
                    regularMember1.getId()
            ));


            // Then
            // Überprüfen, ob das Mitglied gelöscht wurde
            assertThat(projectMemberRepository.count()).isEqualTo(initialMemberCount - 1);
            assertThat(projectMemberRepository.findById(regularMember1.getId())).isEmpty();

            // Überprüfen, ob der zugehörige Invite gelöscht wurde
            assertThat(projectInviteRepository.count()).isEqualTo(initialInviteCount - 1);
        }

        @Test
        @DisplayName("Should throw exception when regular user tries to remove another member")
        void shouldThrowExceptionWhenRegularUserTriesToRemoveMember() {
            // When & Then
            assertThatThrownBy(() -> projectMemberService.removeMemberFromProject(
                    regularUser1.getId(),
                    testProject.getId(),
                    regularMember2.getId()
            )).isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        @DisplayName("Should throw exception when user tries to remove self")
        void shouldThrowExceptionWhenUserTriesToRemoveSelf() {
            // When & Then
            assertThatThrownBy(() -> projectMemberService.removeMemberFromProject(
                    ownerUser.getId(),
                    testProject.getId(),
                    ownerMember.getId() // ID des Owners (Selbst-Entfernung)
            )).isInstanceOf(InvalidMemberRemovalException.class);
        }

        @Test
        @DisplayName("Should throw exception when trying to remove the owner (by another user)")
        void shouldThrowExceptionWhenTryingToRemoveOwner() {

            UserEntity adminUser = testDataFactory.createTestUser("admin999", "admin");
            ProjectMember adminMember = testDataFactory.createTestMember(testProject, adminUser); // Erstellt mit BASE

            adminMember.assignRole(testProject.getOwnerRole()); // Weist Owner-Rolle zu


            projectMemberRepository.save(adminMember);

            // When & Then
            assertThatThrownBy(() -> projectMemberService.removeMemberFromProject(
                    adminUser.getId(),    // Der Ausführende (hat Rechte)
                    testProject.getId(),
                    ownerMember.getId()   // Das Ziel (ist Owner)
            ))
                    .isInstanceOf(InvalidMemberRemovalException.class);
        }

        @Test
        @DisplayName("Should throw exception for non-existent member")
        void shouldThrowExceptionForNonExistentMember() {
            // When & Then
            assertThatThrownBy(() -> projectMemberService.removeMemberFromProject(
                    ownerUser.getId(),
                    testProject.getId(),
                    9999L // Falsche Member-ID
            )).isInstanceOf(ProjectMemberNotFoundException.class);
        }
    }
}
