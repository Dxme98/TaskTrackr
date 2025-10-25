package com.dev.tasktrackr.ProjectTests.domain;

import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.domain.basic.BasicDetails;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.Task;
import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Task Entity Tests")
public class TaskTests {
    @Mock
    private BasicDetails mockBasicDetails;
    @Mock
    private ProjectMember mockTaskCreator;
    @Mock
    private ProjectMember mockUpdatedBy;
    @Mock
    private ProjectMember mockAssignedMember1;
    @Mock
    private ProjectMember mockAssignedMember2;
    @Mock
    private CreateTaskRequest mockCreateTaskRequest;

    private Task task;
    private Set<ProjectMember> assignedMembers;
    private LocalDateTime testDueDate;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        testDueDate = LocalDateTime.now().plusDays(7);
        assignedMembers = new HashSet<>();
        assignedMembers.add(mockAssignedMember1);
        assignedMembers.add(mockAssignedMember2);

        when(mockCreateTaskRequest.getTitle()).thenReturn("Test Task");
        when(mockCreateTaskRequest.getDescription()).thenReturn("Test Description");
        when(mockCreateTaskRequest.getPriority()).thenReturn(Priority.HIGH);
        when(mockCreateTaskRequest.getDueDate()).thenReturn(testDueDate);

        task = Task.builder()
                .id(1L)
                .basicDetails(mockBasicDetails)
                .title("Test Task")
                .description("Test Description")
                .priority(Priority.HIGH)
                .status(Status.IN_PROGRESS)
                .dueDate(testDueDate)
                .createdBy(mockTaskCreator)
                .updatedBy(mockUpdatedBy)
                .assignedMembers(assignedMembers)
                .build();
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create task with builder")
        void shouldCreateTaskWithBuilder() {
            assertEquals(1L, task.getId());
            assertEquals(mockBasicDetails, task.getBasicDetails());
            assertEquals("Test Task", task.getTitle());
            assertEquals("Test Description", task.getDescription());
            assertEquals(Priority.HIGH, task.getPriority());
            assertEquals(Status.IN_PROGRESS, task.getStatus());
            assertEquals(testDueDate, task.getDueDate());
            assertEquals(mockTaskCreator, task.getCreatedBy());
            assertEquals(mockUpdatedBy, task.getUpdatedBy());
            assertEquals(assignedMembers, task.getAssignedMembers());
        }

        @Test
        @DisplayName("Should create task with static factory method")
        void shouldCreateTaskWithStaticFactoryMethod() {
            Task createdTask = Task.create(mockCreateTaskRequest, mockBasicDetails, mockTaskCreator, assignedMembers);

            assertEquals(mockBasicDetails, createdTask.getBasicDetails());
            assertEquals("Test Task", createdTask.getTitle());
            assertEquals("Test Description", createdTask.getDescription());
            assertEquals(Priority.HIGH, createdTask.getPriority());
            assertEquals(testDueDate, createdTask.getDueDate());
            assertEquals(mockTaskCreator, createdTask.getCreatedBy());
            assertEquals(assignedMembers, createdTask.getAssignedMembers());
        }

        @Nested
        @DisplayName("Assignment Tests")
        class AssignmentTests {

            @Test
            @DisplayName("Should handle empty assigned members set")
            void shouldHandleEmptyAssignedMembersSet() {
                Task taskWithNoMembers = Task.create(mockCreateTaskRequest, mockBasicDetails, mockTaskCreator, new HashSet<>());

                assertNotNull(taskWithNoMembers.getAssignedMembers());
                assertTrue(taskWithNoMembers.getAssignedMembers().isEmpty());
            }

            @Test
            @DisplayName("Should handle null assigned members")
            void shouldHandleNullAssignedMembers() {
                Task taskWithNullMembers = Task.create(mockCreateTaskRequest, mockBasicDetails, mockTaskCreator, null);

                assertNull(taskWithNullMembers.getAssignedMembers());
            }

            @Test
            @DisplayName("Should preserve assigned members")
            void shouldPreserveAssignedMembers() {
                Task createdTask = Task.create(mockCreateTaskRequest, mockBasicDetails, mockTaskCreator, assignedMembers);

                assertEquals(2, createdTask.getAssignedMembers().size());
                assertTrue(createdTask.getAssignedMembers().contains(mockAssignedMember1));
                assertTrue(createdTask.getAssignedMembers().contains(mockAssignedMember2));
            }
        }

        @Nested
        @DisplayName("Field Validation Tests")
        class FieldValidationTests {

            @Test
            @DisplayName("Should handle null due date")
            void shouldHandleNullDueDate() {
                when(mockCreateTaskRequest.getDueDate()).thenReturn(null);

                Task taskWithoutDueDate = Task.create(mockCreateTaskRequest, mockBasicDetails, mockTaskCreator, assignedMembers);

                assertNull(taskWithoutDueDate.getDueDate());
            }

            @Test
            @DisplayName("Should preserve all priority types")
            void shouldPreserveAllPriorityTypes() {
                for (Priority priority : Priority.values()) {
                    when(mockCreateTaskRequest.getPriority()).thenReturn(priority);

                    Task taskWithPriority = Task.create(mockCreateTaskRequest, mockBasicDetails, mockTaskCreator, assignedMembers);

                    assertEquals(priority, taskWithPriority.getPriority());
                }
            }
        }
    }}
