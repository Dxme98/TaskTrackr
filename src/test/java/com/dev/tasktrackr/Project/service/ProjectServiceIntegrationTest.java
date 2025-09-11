package com.dev.tasktrackr.Project.service;

import com.dev.tasktrackr.BaseIntegrationTest;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.service.ProjectServiceImpl;
import com.dev.tasktrackr.shared.exception.custom.UserNotFoundException;
import com.dev.tasktrackr.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

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
        @DisplayName("Should create BASIC project successfully")
        @Rollback
        void shouldCreateBasicProjectSuccessfully() {
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
        void shouldThrowIfUserDoesNotExists() {
            ProjectRequest request = new ProjectRequest("Basic Test Project", ProjectType.BASIC);
            assertThrows(UserNotFoundException.class, () -> projectService.createProject("NonExistingID", request));
        }
    }
}
