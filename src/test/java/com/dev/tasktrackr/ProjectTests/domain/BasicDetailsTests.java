package com.dev.tasktrackr.ProjectTests.domain;

import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.domain.*;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.ProjectMemberNotAllowedToCompleteTaskException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BasicDetails Entity Tests")
public class BasicDetailsTests {
    @Mock
    private Project mockProject;
    @Mock
    private CreateTaskRequest mockCreateTaskRequest;
    @Mock
    private ProjectMember mockTaskCreator;
    @Mock
    private ProjectMember mockAssignedMember1;
    @Mock
    private ProjectMember mockAssignedMember2;
    @Mock
    private Task mockTask1;
    @Mock
    private Task mockTask2;
    @Mock
    private Task mockCompletedTask;
    @Mock
    private Information mockInformation;
    @Mock
    private Link mockLink;

    private BasicDetails basicDetails;
    private Set<ProjectMember> assignedMembers;
    private Set<Task> tasks;
    private Set<Link> links;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        assignedMembers = new HashSet<>();
        assignedMembers.add(mockAssignedMember1);
        assignedMembers.add(mockAssignedMember2);

        tasks = new HashSet<>();
        tasks.add(mockTask1);
        tasks.add(mockTask2);

        links = new HashSet<>();
        links.add(mockLink);

        when(mockTaskCreator.getId()).thenReturn(100L);
        when(mockAssignedMember1.getId()).thenReturn(1L);
        when(mockAssignedMember2.getId()).thenReturn(2L);
        when(mockTask1.getId()).thenReturn(10L);
        when(mockTask2.getId()).thenReturn(20L);

        basicDetails = new BasicDetails(1L, mockProject, tasks, links, mockInformation);
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create BasicDetails with project constructor")
        void shouldCreateBasicDetailsWithProjectConstructor() {
            BasicDetails details = new BasicDetails(mockProject);

            assertEquals(mockProject, details.getProject());
            assertNotNull(details.getTasks());
            assertNotNull(details.getLinks());
            assertTrue(details.getTasks().isEmpty());
            assertTrue(details.getLinks().isEmpty());
        }
    }

    @Nested
    @DisplayName("Task Management Tests")
    class TaskManagementTests {

        @Test
        @DisplayName("Should add task successfully")
        void shouldAddTaskSuccessfully() {
            BasicDetails details = new BasicDetails(mockProject);
            Task mockNewTask = mock(Task.class);

            try (MockedStatic<Task> taskMock = mockStatic(Task.class)) {
                taskMock.when(() -> Task.create(mockCreateTaskRequest, details, mockTaskCreator, assignedMembers))
                        .thenReturn(mockNewTask);

                Task result = details.addTask(mockCreateTaskRequest, mockTaskCreator, assignedMembers);

                assertEquals(mockNewTask, result);
                assertTrue(details.getTasks().contains(mockNewTask));
                taskMock.verify(() -> Task.create(mockCreateTaskRequest, details, mockTaskCreator, assignedMembers));
            }
        }

        @Test
        @DisplayName("Should find task by id successfully")
        void shouldFindTaskByIdSuccessfully() {
            Task foundTask = basicDetails.findTask(10L);

            assertEquals(mockTask1, foundTask);
        }

        @Test
        @DisplayName("Should throw TaskNotFoundException when task not found")
        void shouldThrowTaskNotFoundExceptionWhenTaskNotFound() {
            Long nonExistentTaskId = 999L;

            TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                    () -> basicDetails.findTask(nonExistentTaskId));

            assertNotNull(exception);
        }

        @Test
        @DisplayName("Should delete task successfully")
        void shouldDeleteTaskSuccessfully() {
            int initialTaskCount = basicDetails.getTasks().size();

            basicDetails.deleteTask(10L);

            assertEquals(initialTaskCount - 1, basicDetails.getTasks().size());
            assertFalse(basicDetails.getTasks().contains(mockTask1));
        }

        @Test
        @DisplayName("Should throw exception when trying to delete non-existent task")
        void shouldThrowExceptionWhenTryingToDeleteNonExistentTask() {
            Long nonExistentTaskId = 999L;

            assertThrows(TaskNotFoundException.class, () -> basicDetails.deleteTask(nonExistentTaskId));
        }
    }

    @Nested
    @DisplayName("Task Completion Tests")
    class TaskCompletionTests {

        @BeforeEach
        void setupForCompletionTests() {
            // Setup mock task with assigned members and creator
            Set<ProjectMember> taskAssignedMembers = new HashSet<>();
            taskAssignedMembers.add(mockAssignedMember1);
            taskAssignedMembers.add(mockAssignedMember2);

            when(mockTask1.getAssignedMembers()).thenReturn(taskAssignedMembers);
            when(mockTask1.getCreatedBy()).thenReturn(mockTaskCreator);
            when(mockTask1.complete()).thenReturn(mockCompletedTask);
        }

        @Test
        @DisplayName("Should complete task when member is the creator")
        void shouldCompleteTaskWhenMemberIsTheCreator() {
            Long taskId = 10L;
            Long creatorId = 100L;

            Task result = basicDetails.completeTask(taskId, creatorId);

            assertEquals(mockCompletedTask, result);
            verify(mockTask1).complete();
        }

        @Test
        @DisplayName("Should complete task when member is assigned")
        void shouldCompleteTaskWhenMemberIsAssigned() {
            Long taskId = 10L;
            Long assignedMemberId = 1L;

            Task result = basicDetails.completeTask(taskId, assignedMemberId);

            assertEquals(mockCompletedTask, result);
            verify(mockTask1).complete();
        }

        @Test
        @DisplayName("Should complete task when member is both creator and assigned")
        void shouldCompleteTaskWhenMemberIsBothCreatorAndAssigned() {
            // Setup where creator is also in assigned members
            Set<ProjectMember> taskAssignedMembers = new HashSet<>();
            taskAssignedMembers.add(mockTaskCreator);
            taskAssignedMembers.add(mockAssignedMember1);

            when(mockTask1.getAssignedMembers()).thenReturn(taskAssignedMembers);
            when(mockTask1.getCreatedBy()).thenReturn(mockTaskCreator);
            when(mockTask1.complete()).thenReturn(mockCompletedTask);

            Long taskId = 10L;
            Long creatorId = 100L;

            Task result = basicDetails.completeTask(taskId, creatorId);

            assertEquals(mockCompletedTask, result);
            verify(mockTask1).complete();
        }

        @Test
        @DisplayName("Should throw exception when member is neither creator nor assigned")
        void shouldThrowExceptionWhenMemberIsNeitherCreatorNorAssigned() {
            Long taskId = 10L;
            Long unauthorizedMemberId = 999L;

            assertThrows(ProjectMemberNotAllowedToCompleteTaskException.class,
                    () -> basicDetails.completeTask(taskId, unauthorizedMemberId));

            verify(mockTask1, never()).complete();
        }

        @Test
        @DisplayName("Should throw exception when task not found for completion")
        void shouldThrowExceptionWhenTaskNotFoundForCompletion() {
            Long nonExistentTaskId = 999L;
            Long memberId = 1L;

            assertThrows(TaskNotFoundException.class,
                    () -> basicDetails.completeTask(nonExistentTaskId, memberId));
        }
    }
}