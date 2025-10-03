package com.dev.tasktrackr.ProjectTests.service;

import com.dev.tasktrackr.BaseIntegrationTest;
import com.dev.tasktrackr.activity.*;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ProjectActivityService Integration Tests")
public class ProjectActivityServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProjectActivityServiceImpl projectActivityService;

    @Autowired
    private ProjectActivityRepository projectActivityRepository;

    private UserEntity testUser;
    private UserEntity anotherUser;
    private Project testProject;

    private Long ownerMemberId;
    private Long anotherMemberId;

    @BeforeEach
    void setUp() {
        testUser = createTestUser("user-activity-1", "activityUser1");
        anotherUser = createTestUser("user-activity-2", "activityUser2");
        testProject = createTestProject("Activity Project", ProjectType.BASIC, testUser);

        ownerMemberId = testProject.findProjectMember(testUser.getId()).getId();

        testProject.addMember(anotherUser);
        projectRepository.save(testProject);

        anotherMemberId = testProject.findProjectMember(anotherUser.getId()).getId();
    }

    @Nested
    @DisplayName("Record Activity Tests")
    class RecordActivityTests {

        @Test
        @DisplayName("Should record TaskCompletedEvent successfully")
        @Rollback
        void shouldRecordTaskCompletedEvent() {
            // Given: Ein Event, das den Abschluss einer Aufgabe repräsentiert
            var event = new ProjectActivityEvents.TaskCompletedEvent(
                    testProject.getId(),
                    ownerMemberId,
                    testUser.getUsername(),
                    99L, // Task-ID
                    "Finish documentation" // Task-Titel
            );

            // When: Das Event wird verarbeitet und eine Aktivität aufgezeichnet
            projectActivityService.recordActivity(event.toActivityParameter());

            // Then: Die Aktivität wird korrekt in der Datenbank gespeichert
            List<ProjectActivity> activities = projectActivityRepository.findByProjectId(testProject.getId());
            assertThat(activities).hasSize(1);

            ProjectActivity savedActivity = activities.get(0);
            assertEquals(ActivityType.TASK_COMPLETED, savedActivity.getActivityType());
            assertEquals(testUser.getUsername(), savedActivity.getActorName());
            assertEquals(99L, savedActivity.getTargetId());
            assertEquals("Finish documentation", savedActivity.getTargetName());
            assertEquals(TargetType.TASK, savedActivity.getTargetType());
        }

        @Test
        @DisplayName("Should record ProjectNameChangedEvent with JSON context")
        @Rollback
        void shouldRecordProjectNameChangedEventWithJsonContext() {
            // Given: Ein Event für die Änderung des Projektnamens
            var event = new ProjectActivityEvents.ProjectNameChangedEvent(
                    testProject.getId(),
                    anotherMemberId,
                    anotherUser.getUsername(),
                    "Old Project Name",
                    "New Awesome Project Name"
            );
            String expectedJsonContext = "{\"oldName\":\"Old Project Name\",\"newName\":\"New Awesome Project Name\"}";


            // When: Das Event wird verarbeitet
            projectActivityService.recordActivity(event.toActivityParameter());

            // Then: Die Aktivität inklusive des JSON-Kontexts wird korrekt gespeichert
            ProjectActivity savedActivity = projectActivityRepository.findByProjectId(testProject.getId()).get(0);
            assertEquals(ActivityType.CHANGED_PROJECT_NAME, savedActivity.getActivityType());
            assertEquals(anotherUser.getUsername(), savedActivity.getActorName());
            assertEquals(expectedJsonContext, savedActivity.getContext());
        }
    }

    @Nested
    @DisplayName("Find Activities Tests")
    class FindActivitiesTests {

        @BeforeEach
        void setUpActivities() {
            // Given: Erstelle Test-Aktivitäten mithilfe der neuen Event-Objekte
            var event1 = new ProjectActivityEvents.TaskCreatedEvent(testProject.getId(), ownerMemberId, testUser.getUsername(), 101L, "Initial task");
            var event2 = new ProjectActivityEvents.UserJoinedProjectEvent(testProject.getId(), anotherMemberId, anotherUser.getUsername());

            projectActivityService.recordActivity(event1.toActivityParameter());
            projectActivityService.recordActivity(event2.toActivityParameter());
        }

        @Test
        @DisplayName("Should find activities by project ID with pagination")
        @Rollback
        void shouldFindActivitiesByProjectIdWithPagination() {
            // Given
            Pageable pageRequest = PageRequest.of(0, 10);

            // When
            Page<ProjectActivityDto> result = projectActivityService.findActivitiesByProjectId(testUser.getId(), testProject.getId(), pageRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2); // Nur die 2 Aktivitäten von testProject
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(ProjectActivityDto::getActorName)
                    .containsExactlyInAnyOrder(testUser.getUsername(), anotherUser.getUsername());
        }

        @Test
        @DisplayName("Should return empty page for project with no activities")
        @Rollback
        void shouldReturnEmptyPageForProjectWithNoActivities() {
            // Given
            Project projectWithoutActivities = createTestProject("Empty Project", ProjectType.BASIC, testUser);
            Pageable pageRequest = PageRequest.of(0, 10);

            // When
            Page<ProjectActivityDto> result = projectActivityService.findActivitiesByProjectId(testUser.getId(), projectWithoutActivities.getId(), pageRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }
    }
}
