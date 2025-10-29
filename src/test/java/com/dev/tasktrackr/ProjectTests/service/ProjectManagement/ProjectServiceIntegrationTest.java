package com.dev.tasktrackr.ProjectTests.service.ProjectManagement;

import com.dev.tasktrackr.ProjectTests.service.shared.ProjectManagementBaseTest;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.service.*;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
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
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectService Integration Tests")
public class ProjectServiceIntegrationTest extends ProjectManagementBaseTest {

    @Autowired
    private ProjectService projectService;

    private UserEntity testUser;
    private UserEntity anotherUser;

    @BeforeEach
    void setUp() {
        testUser = createTestUser("user123", "testuser");
        anotherUser = createTestUser("user456", "anotheruser");
    }

    @Nested
    @DisplayName("Create Project Tests")
    class CreateProjectTests {

        @Test
        @DisplayName("Should create project successfully")
        void shouldCreateProjectSuccessfully() {
            // Given
            ProjectRequest request = new ProjectRequest("Basic Test Project", ProjectType.BASIC);

            // When
            ProjectOverviewDto result = projectService.createProject(testUser.getId(), request);

            // Then
            assertNotNull(result);
            assertEquals("Basic Test Project", result.getName());
            assertEquals(ProjectType.BASIC, result.getProjectType());
            assertNotNull(result.getCreatedAt());

            // Verify in database
            Project savedProject = projectRepository.findById(result.getId()).orElseThrow();
            assertEquals(1, savedProject.getProjectMembers().size());
            assertEquals(2, savedProject.getProjectRoles().size()); // OWNER + BASE
        }

        @Test
        @DisplayName("Should throw exception for non-existent user")
        void shouldThrowIfUserDoesNotExists() {
            ProjectRequest request = new ProjectRequest("Basic Test Project", ProjectType.BASIC);
            assertThrows(UserNotFoundException.class, () -> projectService.createProject("NonExistingID", request));
        }

        @Test
        @DisplayName("Should initialize owner as project member")
        void shouldInitializeOwnerAsProjectMember() {
            // Given
            ProjectRequest request = new ProjectRequest("Owner Test Project", ProjectType.BASIC);

            // When
            ProjectOverviewDto result = projectService.createProject(testUser.getId(), request);

            // Then
            Project savedProject = projectRepository.findById(result.getId()).orElseThrow();
            ProjectMember ownerMember = savedProject.getProjectMembers().iterator().next();

            assertEquals(1, savedProject.getProjectMembers().size());
            assertEquals(ownerMember.getUser().getId(), testUser.getId());
            assertEquals(RoleType.OWNER, ownerMember.getProjectRole().getRoleType());
        }
    }

    @Nested
    @DisplayName("Find Projects Tests")
    class FindProjectsTest {

        @BeforeEach
        void setUpProjects() {
            createTestProject("Project 1", ProjectType.BASIC, testUser);
            createTestProject("Project 2", ProjectType.SCRUM, testUser);
            createTestProject("Other Project", ProjectType.BASIC, anotherUser);
        }

        @Test
        @DisplayName("Should find projects by user ID with pagination")
        void shouldFindProjectsByUserIdWithPagination() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10);

            // When
            Page<ProjectOverviewDto> result = projectService.findProjectsByUserId(testUser.getId(), pageRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(ProjectOverviewDto::getName)
                    .containsExactlyInAnyOrder("Project 1", "Project 2");
        }

        @Test
        @DisplayName("Should return empty page for user with no projects")
        void shouldReturnEmptyPageForUserWithNoProjects() {
            // Given
            UserEntity userWithoutProjects = createTestUser("noproject123", "noprojects");
            PageRequest pageRequest = PageRequest.of(0, 10);

            // When
            Page<ProjectOverviewDto> result = projectService.findProjectsByUserId(userWithoutProjects.getId(), pageRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Get Project Details Tests")
    class GetProjectDetailsTests {

        private Project project;

        @BeforeEach
        void setUpProject() {
            project = createTestProject("Detail Project", ProjectType.BASIC, testUser);
        }

        @Test
        @DisplayName("Should get project details successfully if user is member")
        void shouldGetProjectDetailsSuccessfully() {
            // When
            ProjectOverviewDto result = projectService.getProjectDetails(project.getId(), testUser.getId());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Detail Project");
            assertThat(result.getId()).isEqualTo(project.getId());
        }

        @Test
        @DisplayName("Should throw exception if user is not a project member")
        void shouldThrowExceptionIfUserIsNotProjectMember() {
            assertThrows(UserNotProjectMemberException.class, // Annahme, dass checkProjectMemberShip dies wirft
                    () -> projectService.getProjectDetails(project.getId(), anotherUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception if project does not exist")
        void shouldThrowExceptionIfProjectDoesNotExist() {
            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> projectService.getProjectDetails(999L, testUser.getId()));
        }
    }
}