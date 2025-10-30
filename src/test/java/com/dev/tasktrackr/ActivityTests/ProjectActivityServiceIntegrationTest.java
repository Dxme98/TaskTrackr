package com.dev.tasktrackr.ActivityTests;

import com.dev.tasktrackr.ProjectTests.service.shared.ProjectManagementBaseTest;
import com.dev.tasktrackr.activity.*;
import com.dev.tasktrackr.activity.domain.ProjectActivity;
import com.dev.tasktrackr.activity.repository.ProjectActivityRepository;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.repository.ProjectMemberRepository;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("ProjectActivityService Integration Tests")
public class ProjectActivityServiceIntegrationTest extends ProjectManagementBaseTest {

    @Autowired
    private ProjectActivityServiceImpl projectActivityService;

    @Autowired
    private ProjectActivityRepository projectActivityRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    private UserEntity testUser;
    private UserEntity anotherUser;
    private Project testProject;

    private Long ownerMemberId;
    private Long anotherMemberId;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createTestUser("user-activity-1", "activityUser1");
        anotherUser = testDataFactory.createTestUser("user-activity-2", "activityUser2");
        testProject = testDataFactory.createTestProject("Activity Project", ProjectType.BASIC, testUser);

        ProjectMember ownerMember = projectMemberRepository.findProjectMemberByUserIdAndProjectId(testUser.getId(), testProject.getId())
                .orElseThrow(() -> new IllegalStateException("Owner-Mitglied wurde nicht korrekt erstellt."));
        ownerMemberId = ownerMember.getId();

        ProjectMember anotherMember = testDataFactory.createTestMember(testProject, anotherUser);
        anotherMemberId = anotherMember.getId();
    }

    @Nested
    @DisplayName("Record Activity Tests")
    class RecordActivityTests {

        @Test
        @DisplayName("Should record TaskCompletedEvent successfully")
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
        void shouldReturnEmptyPageForProjectWithNoActivities() {
            // Given
            Project projectWithoutActivities = testDataFactory.createTestProject("Empty Project", ProjectType.BASIC, testUser);
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