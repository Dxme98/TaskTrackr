package com.dev.tasktrackr.ProjectTests.domain;

import com.dev.tasktrackr.project.api.dtos.request.CreateLinkRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.project.domain.*;
import com.dev.tasktrackr.project.domain.basic.BasicDetails;
import com.dev.tasktrackr.project.domain.basic.Information;
import com.dev.tasktrackr.project.domain.basic.Link;
import com.dev.tasktrackr.project.domain.basic.LinkType;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.ProjectMemberNotAllowedToCompleteTaskException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.LinkNotFoundException;
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

/**
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
    private Link mockLink1;
    @Mock
    private Link mockLink2;

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
        links.add(mockLink1);
        links.add(mockLink2);

        when(mockTaskCreator.getId()).thenReturn(100L);
        when(mockAssignedMember1.getId()).thenReturn(1L);
        when(mockAssignedMember2.getId()).thenReturn(2L);
        when(mockTask1.getId()).thenReturn(10L);
        when(mockTask2.getId()).thenReturn(20L);
        when(mockLink1.getId()).thenReturn(30L);
        when(mockLink2.getId()).thenReturn(40L);

        basicDetails = new BasicDetails(1L, mockProject, tasks, links, new Information(null));
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
        @DisplayName("Should find task by object successfully")
        void shouldFindTaskByObjectSuccessfully() {
            Task foundTask = basicDetails.findTask(mockTask1);

            assertEquals(mockTask1, foundTask);
        }

        @Test
        @DisplayName("Should throw TaskNotFoundException when task not found by id")
        void shouldThrowTaskNotFoundExceptionWhenTaskNotFoundById() {
            Long nonExistentTaskId = 999L;

            TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                    () -> basicDetails.findTask(nonExistentTaskId));

            assertNotNull(exception);
        }

        @Test
        @DisplayName("Should throw TaskNotFoundException when task not found by object")
        void shouldThrowTaskNotFoundExceptionWhenTaskNotFoundByObject() {
            Task nonExistentTask = mock(Task.class);
            when(nonExistentTask.getId()).thenReturn(999L);

            TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                    () -> basicDetails.findTask(nonExistentTask));

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

    @Nested
    @DisplayName("Information Management Tests")
    class InformationManagementTests {

        @Test
        @DisplayName("Should update information content successfully")
        void shouldUpdateInformationContentSuccessfully() {
            // Given
            BasicDetails details = new BasicDetails(mockProject);
            String updatedContent = "Updated information content";
            UpdateInformationContentRequest request = new UpdateInformationContentRequest(updatedContent);

            // When
            Information result = details.updateInformationContent(request);

            // Then
            assertNotNull(result);
            assertEquals(updatedContent, result.getContent());
            assertEquals(details, result.getBasicDetails());
        }

        @Test
        @DisplayName("Should update information with empty content")
        void shouldUpdateInformationWithEmptyContent() {
            // Given
            BasicDetails details = new BasicDetails(mockProject);
            String emptyContent = "";
            UpdateInformationContentRequest request = new UpdateInformationContentRequest(emptyContent);

            // When
            Information result = details.updateInformationContent(request);

            // Then
            assertNotNull(result);
            assertEquals(emptyContent, result.getContent());
        }

        @Test
        @DisplayName("Should update information with large content")
        void shouldUpdateInformationWithLargeContent() {
            // Given
            BasicDetails details = new BasicDetails(mockProject);
            String largeContent = "x".repeat(50000); // 50k characters
            UpdateInformationContentRequest request = new UpdateInformationContentRequest(largeContent);

            // When
            Information result = details.updateInformationContent(request);

            // Then
            assertNotNull(result);
            assertEquals(largeContent, result.getContent());
        }

        @Test
        @DisplayName("Should preserve existing information object when updating")
        void shouldPreserveExistingInformationObjectWhenUpdating() {
            // Given
            BasicDetails details = new BasicDetails(mockProject);
            Information originalInformation = details.getInformation();

            String firstContent = "First content";
            String secondContent = "Second content";

            UpdateInformationContentRequest firstRequest = new UpdateInformationContentRequest(firstContent);
            UpdateInformationContentRequest secondRequest = new UpdateInformationContentRequest(secondContent);

            // When
            Information firstResult = details.updateInformationContent(firstRequest);
            Information secondResult = details.updateInformationContent(secondRequest);

            // Then
            assertSame(originalInformation, firstResult);
            assertSame(originalInformation, secondResult);
            assertEquals(secondContent, details.getInformation().getContent());
        }
    }

    @Nested
    @DisplayName("Link Management Tests")
    class LinkManagementTests {

        @Test
        @DisplayName("Should add link successfully")
        void shouldAddLinkSuccessfully() {
            // Given
            BasicDetails details = new BasicDetails(mockProject);

            String title = "Test Link";
            String url = "https://example.com";
            LinkType linkType = LinkType.DOCS;
            CreateLinkRequest request = new CreateLinkRequest(title, url, linkType);

            // When
            Link result = details.addLink(request);

            // Then
            assertNotNull(result);
            assertEquals(title, result.getTitle());
            assertEquals(url, result.getUrl());
            assertEquals(linkType, result.getType());
            assertEquals(details, result.getBasicDetails());
            assertTrue(details.getLinks().contains(result));
            assertEquals(1, details.getLinks().size());
        }

        @Test
        @DisplayName("Should find link by id successfully")
        void shouldFindLinkByIdSuccessfully() {
            // When
            Link foundLink = basicDetails.findLink(30L);

            // Then
            assertEquals(mockLink1, foundLink);
        }

        @Test
        @DisplayName("Should throw LinkNotFoundException when link not found")
        void shouldThrowLinkNotFoundExceptionWhenLinkNotFound() {
            // Given
            Long nonExistentLinkId = 999L;

            // When/Then
            LinkNotFoundException exception = assertThrows(LinkNotFoundException.class,
                    () -> basicDetails.findLink(nonExistentLinkId));

            assertNotNull(exception);
        }

        @Test
        @DisplayName("Should delete link successfully")
        void shouldDeleteLinkSuccessfully() {
            // Given
            int initialLinkCount = basicDetails.getLinks().size();

            // When
            basicDetails.deleteLink(30L);

            // Then
            assertEquals(initialLinkCount - 1, basicDetails.getLinks().size());
            assertFalse(basicDetails.getLinks().contains(mockLink1));
        }

        @Test
        @DisplayName("Should throw exception when trying to delete non-existent link")
        void shouldThrowExceptionWhenTryingToDeleteNonExistentLink() {
            // Given
            Long nonExistentLinkId = 999L;

            // When/Then
            assertThrows(LinkNotFoundException.class, () -> basicDetails.deleteLink(nonExistentLinkId));
        }

        @Test
        @DisplayName("Should add multiple links successfully")
        void shouldAddMultipleLinksSuccessfully() {
            // Given
            BasicDetails details = new BasicDetails(mockProject);

            CreateLinkRequest request1 = new CreateLinkRequest("Link 1", "https://example1.com", LinkType.DOCS);
            CreateLinkRequest request2 = new CreateLinkRequest("Link 2", "https://example2.com", LinkType.GITHUB);

            // When
            Link result1 = details.addLink(request1);
            Link result2 = details.addLink(request2);

            // Then
            assertNotNull(result1);
            assertNotNull(result2);
            assertEquals("Link 1", result1.getTitle());
            assertEquals("Link 2", result2.getTitle());
            assertEquals(2, details.getLinks().size());
            assertTrue(details.getLinks().contains(result1));
            assertTrue(details.getLinks().contains(result2));
        }

        @Test
        @DisplayName("Should handle different link types")
        void shouldHandleDifferentLinkTypes() {
            // Given
            BasicDetails details = new BasicDetails(mockProject);

            // When/Then
            for (LinkType linkType : LinkType.values()) {
                String title = "Link for " + linkType.name();
                String url = "https://example-" + linkType.name().toLowerCase() + ".com";
                CreateLinkRequest request = new CreateLinkRequest(title, url, linkType);

                Link result = details.addLink(request);

                assertNotNull(result);
                assertEquals(linkType, result.getType());
                assertEquals(title, result.getTitle());
                assertEquals(url, result.getUrl());
                assertTrue(details.getLinks().contains(result));
            }

            assertEquals(LinkType.values().length, details.getLinks().size());
        }
    }
}
 */