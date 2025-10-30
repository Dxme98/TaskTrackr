package com.dev.tasktrackr.ProjectTests.service.Basic;

import com.dev.tasktrackr.ProjectTests.service.shared.BasicDetailsBaseTest;
import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.Task;
import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.enums.Status;
import com.dev.tasktrackr.project.repository.ProjectMemberRepository;
import com.dev.tasktrackr.project.repository.TaskRepository;
import com.dev.tasktrackr.project.service.TaskService;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.ProjectMemberNotAllowedToCompleteTaskException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.TaskNotFoundException;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskService Integration Tests")
public class TaskServiceIntegrationTests extends BasicDetailsBaseTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository; // Für Verify-Asserts

    @Autowired
    private ProjectMemberRepository projectMemberRepository; // Für setUp

    private UserEntity ownerUser;
    private UserEntity anotherUser;
    private UserEntity thirdUser;
    private UserEntity nonMemberUser;
    private Project testProject;
    private ProjectMember ownerMember;
    private ProjectMember anotherMember;
    private ProjectMember thirdMember;

    @BeforeEach
    void setUp() {
        ownerUser = testDataFactory.createTestUser("user123", "testuser");
        anotherUser = testDataFactory.createTestUser("user456", "anotheruser");
        thirdUser = testDataFactory.createTestUser("user789", "thirduser");
        nonMemberUser = testDataFactory.createTestUser("user999", "nonmember");

        testProject = testDataFactory.createTestProject("Test Project", ProjectType.BASIC, ownerUser);

        ownerMember = projectMemberRepository.findProjectMemberByUserIdAndProjectId(ownerUser.getId(), testProject.getId())
                .orElseThrow(() -> new IllegalStateException("Owner-Mitglied wurde nicht korrekt erstellt."));
        anotherMember = testDataFactory.createTestMember(testProject, anotherUser);
        thirdMember = testDataFactory.createTestMember(testProject, thirdUser);
    }

    @Nested
    @DisplayName("Create Task Tests")
    class CreateTaskTests {

        @Test
        @DisplayName("Should create task successfully")
        void shouldCreateTaskSuccessfully() {
            // Given
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test Task")
                    .description("Test Description")
                    .priority(Priority.HIGH)
                    .dueDate(LocalDateTime.now().plusDays(7))
                    .assignedToMemberIds(new HashSet<>(Set.of(thirdMember.getId()))) // FIX
                    .build();

            long initialTaskCount = taskRepository.count();

            // When
            TaskResponseDto result = taskService.createTask(testProject.getId(), request, ownerUser.getId());

            // Then
            assertNotNull(result);
            assertEquals("Test Task", result.getTitle());
            assertEquals(Priority.HIGH, result.getPriority());
            assertEquals(Status.IN_PROGRESS, result.getStatus());
            assertEquals(ownerMember.getId(), result.getCreatedBy().getId());
            assertEquals(1, result.getAssignedToMembers().size());

            // Verify in database
            assertEquals(initialTaskCount + 1, taskRepository.count());
            Task savedTask = taskRepository.findById(result.getId()).orElseThrow();
            assertEquals("Test Task", savedTask.getTitle());
            assertEquals(1, savedTask.getAssignedMembers().size());
        }

        @Test
        @DisplayName("Should throw exception for non-existent project")
        void shouldThrowExceptionForNonExistentProject() {
            // Given
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test Task")
                    .assignedToMemberIds(new HashSet<>(Set.of(anotherMember.getId()))) // FIX
                    .build();

            // When/Then
            assertThrows(ProjectNotFoundException.class,
                    () -> taskService.createTask(999L, request, ownerUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception for non-member user")
        void shouldThrowExceptionForNonMemberUser() {
            // Given
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Test Task")
                    .assignedToMemberIds(new HashSet<>(Set.of(anotherMember.getId()))) // FIX
                    .build();

            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> taskService.createTask(testProject.getId(), request, nonMemberUser.getId()));
        }
    }

    @Nested
    @DisplayName("Complete Task Tests")
    class CompleteTaskTests {

        private Task testTask;

        @BeforeEach
        void setUpTask() {
            testTask = testDataFactory.createTestTask(
                    testProject,
                    ownerMember,
                    "Completable Task",
                    new HashSet<>(Set.of(anotherMember)) // FIX
            );
        }

        @Test
        @DisplayName("Should complete task as creator")
        void shouldCompleteTaskAsCreator() {
            // When
            TaskResponseDto result = taskService.completeTask(testProject.getId(), testTask.getId(), ownerUser.getId());

            // Then
            assertNotNull(result);
            assertEquals(Status.COMPLETED, result.getStatus());

            // Verify in database
            Task savedTask = taskRepository.findById(testTask.getId()).orElseThrow();
            assertEquals(Status.COMPLETED, savedTask.getStatus());
        }

        @Test
        @DisplayName("Should complete task as assigned member")
        void shouldCompleteTaskAsAssignedMember() {
            // When
            TaskResponseDto result = taskService.completeTask(testProject.getId(), testTask.getId(), anotherUser.getId());

            // Then
            assertNotNull(result);
            assertEquals(Status.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when non-assigned/non-creator member tries to complete task")
        void shouldThrowExceptionWhenNonAssignedMemberTriesToComplete() {
            // When/Then
            // thirdUser ist weder Creator noch Assigned
            assertThrows(ProjectMemberNotAllowedToCompleteTaskException.class,
                    () -> taskService.completeTask(testProject.getId(), testTask.getId(), thirdUser.getId()));
        }

        @Test
        @DisplayName("Should throw exception for non-existent task")
        void shouldThrowExceptionForNonExistentTask() {
            // When/Then
            assertThrows(TaskNotFoundException.class,
                    () -> taskService.completeTask(testProject.getId(), 999L, ownerUser.getId()));
        }
    }

    @Nested
    @DisplayName("Delete Task Tests")
    class DeleteTaskTests {

        private Task testTask;

        @BeforeEach
        void setUpTask() {
            testTask = testDataFactory.createTestTask(
                    testProject,
                    ownerMember,
                    "Deletable Task",
                    new HashSet<>(Set.of(anotherMember)) // FIX
            );
        }

        @Test
        @DisplayName("Should delete task as owner")
        void shouldDeleteTaskAsOwner() {
            // Given
            long initialTaskCount = taskRepository.count();

            // When
            taskService.deleteTask(testProject.getId(), testTask.getId(), ownerUser.getId());

            // Then
            // Verify in database
            assertEquals(initialTaskCount - 1, taskRepository.count());
            assertFalse(taskRepository.existsById(testTask.getId()));
        }

        @Test
        @DisplayName("Should throw exception when member without Permission tries to delete task")
        void shouldThrowExceptionWhenMemberWithoutPermissionTriesToDeleteTask() {
            // When/Then
            // anotherUser hat die BASE-Rolle (keine BASIC_DELETE_TASK-Rechte)
            assertThrows(PermissionDeniedException.class,
                    () -> taskService.deleteTask(testProject.getId(), testTask.getId(), anotherUser.getId()));
        }
    }

    @Nested
    @DisplayName("Find All Tasks Tests")
    class FindAllTasksTests {

        @BeforeEach
        void setUpTasks() {
            // 1. Abgeschlossene Aufgabe, 'anotherMember' zugewiesen
            Task completedTask = testDataFactory.createTestTask(
                    testProject, ownerMember, "Completed Task", new HashSet<>(Set.of(anotherMember)) // FIX
            );
            completedTask.complete();
            taskRepository.save(completedTask);

            // 2. In-Progress-Aufgabe, 'thirdMember' zugewiesen
            testDataFactory.createTestTask(
                    testProject, ownerMember, "In Progress Task", new HashSet<>(Set.of(thirdMember)) // FIX
            );

            // 3. In-Progress-Aufgabe, 'anotherMember' zugewiesen
            testDataFactory.createTestTask(
                    testProject, ownerMember, "Assigned Task", new HashSet<>(Set.of(anotherMember)) // FIX
            );
        }

        @Test
        @DisplayName("Should find all tasks without filters")
        void shouldFindAllTasksWithoutFilters() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<TaskResponseDto> result = taskService.findAllTasks(
                    testProject.getId(), pageable, ownerUser.getId(), false, null);

            // Then
            assertEquals(3, result.getTotalElements());
        }

        @Test
        @DisplayName("Should find tasks by status")
        void shouldFindTasksByStatus() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When - Find completed tasks
            Page<TaskResponseDto> completedTasks = taskService.findAllTasks(
                    testProject.getId(), pageable, ownerUser.getId(), false, Status.COMPLETED);

            // Then
            assertEquals(1, completedTasks.getTotalElements());
            assertEquals(Status.COMPLETED, completedTasks.getContent().get(0).getStatus());

            // When - Find in-progress tasks
            Page<TaskResponseDto> inProgressTasks = taskService.findAllTasks(
                    testProject.getId(), pageable, ownerUser.getId(), false, Status.IN_PROGRESS);

            // Then
            assertEquals(2, inProgressTasks.getTotalElements());
        }

        @Test
        @DisplayName("Should find tasks assigned to user")
        void shouldFindTasksInProgressAssignedToUser() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When - Finde Tasks, die 'anotherUser' (anotherMember) zugewiesen sind
            Page<TaskResponseDto> assignedTasks = taskService.findAllTasks(
                    testProject.getId(), pageable, anotherUser.getId(), true, null);

            // Then
            assertEquals(1, assignedTasks.getTotalElements());
        }

        @Test
        @DisplayName("Should find tasks assigned to user with no results")
        void shouldFindTasksAssignedToUserWithNoResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When - Finde Tasks, die 'ownerUser' zugewiesen sind (sollte 0 sein)
            Page<TaskResponseDto> assignedTasks = taskService.findAllTasks(
                    testProject.getId(), pageable, ownerUser.getId(), true, null);

            // Then
            assertEquals(0, assignedTasks.getTotalElements());
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void shouldHandlePaginationCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(0, 2, Sort.by("title")); // Sortierung für konsistente Paginierung

            // When
            Page<TaskResponseDto> result = taskService.findAllTasks(
                    testProject.getId(), pageable, ownerUser.getId(), false, null);

            // Then
            assertEquals(3, result.getTotalElements());
            assertEquals(2, result.getSize());
            assertEquals(2, result.getNumberOfElements());
            assertEquals(2, result.getTotalPages());
            assertTrue(result.hasNext());
        }
    }
}

