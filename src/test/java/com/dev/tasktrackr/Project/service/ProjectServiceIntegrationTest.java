package com.dev.tasktrackr.Project.service;

import com.dev.tasktrackr.BaseIntegrationTest;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.RoleType;
import com.dev.tasktrackr.project.service.ProjectServiceImpl;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectService Integration Tests")
public class ProjectServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProjectServiceImpl projectService;

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
        @Rollback
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
            Optional<Project> savedProject = projectRepository.findById(result.getId());
            assertTrue(savedProject.isPresent());
            assertEquals(1, savedProject.get().getProjectMembers().size());
            assertEquals(2, savedProject.get().getProjectRoles().size()); // OWNER + BASE
            assertNotNull(savedProject.get().getId());
        }

        @Test
        @DisplayName("Should throw exception for non-existent user")
        @Rollback
        void shouldThrowIfUserDoesNotExists() {
            ProjectRequest request = new ProjectRequest("Basic Test Project", ProjectType.BASIC);
            assertThrows(UserNotFoundException.class, () -> projectService.createProject("NonExistingID", request));
        }

        @Test
        @DisplayName("Should initialize owner as project member")
        @Rollback
        void shouldInitializeOwnerAsProjectMember() {
            // Given
            ProjectRequest request = new ProjectRequest("Owner Test Project", ProjectType.BASIC);

            // When
            ProjectOverviewDto result = projectService.createProject(testUser.getId(), request);

            // Then
            Project savedProject = projectRepository.findProjectWithInvitesAndMember(result.getId()).get();
            ProjectMember ownerMember = savedProject.getProjectMembers().iterator().next();

            assertEquals(1, savedProject.getProjectMembers().size());
            assertEquals(ownerMember.getUser().getId(), testUser.getId());
            assertEquals(RoleType.OWNER, ownerMember.getProjectRole().getRoleType());
        }
    }

    @Nested
    @DisplayName("Find Projects Tests")
    class findProjectsTest {


            private Project project1;
            private Project project2;
            private Project project3;

            @BeforeEach
            void setUpProjects() {
                project1 = createTestProject("Project 1", ProjectType.BASIC, testUser);
                project2 = createTestProject("Project 2", ProjectType.SCRUM, testUser);
                project3 = createTestProject("Other Project", ProjectType.BASIC, anotherUser);
            }


        @Test
        @DisplayName("Should find projects by user ID with pagination")
        @Rollback
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
        @Rollback
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

        @Test
        @DisplayName("Should handle pagination correctly")
        @Rollback
        void shouldHandlePaginationCorrectly() {
            // Create more projects
            for (int i = 3; i <= 15; i++) {
                createTestProject("Project " + i, ProjectType.BASIC, testUser);
            }
            // Given - Request first page with size 5
            PageRequest firstPage = PageRequest.of(0, 5);
            PageRequest secondPage = PageRequest.of(1, 5);

            // When
            Page<ProjectOverviewDto> page1 = projectService.findProjectsByUserId(testUser.getId(), firstPage);
            Page<ProjectOverviewDto> page2 = projectService.findProjectsByUserId(testUser.getId(), secondPage);

            // Then
            assertThat(page1.getContent()).hasSize(5);
            assertThat(page1.getTotalElements()).isEqualTo(15); // 2 from setup + 13 created
            assertThat(page1.getTotalPages()).isEqualTo(3);
            assertThat(page1.isFirst()).isTrue();
            assertThat(page1.isLast()).isFalse();

            assertThat(page2.getContent()).hasSize(5);
            assertThat(page2.isFirst()).isFalse();
            assertThat(page2.isLast()).isFalse();
        }

        @Test
        @DisplayName("Should only return projects where user is member")
        @Rollback
        void shouldOnlyReturnProjectsWhereUserIsMember() {
            // Given - Add testUser as member to project3
            project3.addMember(testUser);
            projectRepository.save(project3);
            PageRequest pageRequest = PageRequest.of(0, 10);

            // When
            Page<ProjectOverviewDto> result = projectService.findProjectsByUserId(testUser.getId(), pageRequest);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                    .extracting(ProjectOverviewDto::getName)
                    .containsExactlyInAnyOrder("Project 1", "Project 2", "Other Project");
        }
    }
}
