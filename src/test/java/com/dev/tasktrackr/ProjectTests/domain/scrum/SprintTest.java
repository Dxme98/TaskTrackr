package com.dev.tasktrackr.ProjectTests.domain.scrum;

import com.dev.tasktrackr.scrumdetails.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.scrumdetails.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.scrumdetails.domain.*;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.SprintNotActiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Sprint Entität Tests")
public class SprintTest {

    @Mock
    private ScrumDetails mockScrumDetails;

    @Mock
    private CreateSprintRequest mockCreateSprintRequest;

    @Mock
    private UserStory mockUserStory1;

    @Mock
    private UserStory mockUserStory2;

    @Mock
    private SprintBacklogItem mockBacklogItem;

    @Mock
    private SprintSummaryItem mockSummaryItem;

    @Mock
    private ProjectMember mockMember;

    @Mock
    private CreateCommentRequest mockCommentRequest;

    @Mock
    private Comment mockComment;

    private Sprint sprint;
    private final LocalDate startDate = LocalDate.now();
    private final LocalDate endDate = LocalDate.now().plusWeeks(2);

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Standard-Sprint für die meisten Tests
        sprint = Sprint.builder()
                .id(1L)
                .name("Test Sprint")
                .goal("Test Goal")
                .description("Test Description")
                .startDate(startDate)
                .endDate(endDate)
                .status(SprintStatus.PLANNED)
                .scrumDetails(mockScrumDetails)
                .backlogItems(new HashSet<>())
                .sprintSummaryItems(new HashSet<>())
                .build();
    }

    @Nested
    @DisplayName("Erstellungstests")
    class CreationTests {

        @Test
        @DisplayName("Sollte einen Sprint mit der Factory-Methode 'create' erstellen")
        void shouldCreateSprintWithFactoryMethod() {
            // Arrange
            when(mockCreateSprintRequest.getName()).thenReturn("Neuer Sprint");
            when(mockCreateSprintRequest.getGoal()).thenReturn("Neues Goal");
            when(mockCreateSprintRequest.getDescription()).thenReturn("Beschreibung");
            when(mockCreateSprintRequest.getStartDate()).thenReturn(startDate);
            when(mockCreateSprintRequest.getEndDate()).thenReturn(endDate);

            // Act
            Sprint newSprint = Sprint.create(mockCreateSprintRequest, mockScrumDetails);

            // Assert
            assertNotNull(newSprint);
            assertEquals("Neuer Sprint", newSprint.getName());
            assertEquals("Neues Goal", newSprint.getGoal());
            assertEquals("Beschreibung", newSprint.getDescription());
            assertEquals(startDate, newSprint.getStartDate());
            assertEquals(endDate, newSprint.getEndDate());
            assertEquals(SprintStatus.PLANNED, newSprint.getStatus());
            assertEquals(mockScrumDetails, newSprint.getScrumDetails());
            assertTrue(newSprint.getBacklogItems().isEmpty());
            assertTrue(newSprint.getSprintSummaryItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("Zustandsübergangstests (Start/Ende)")
    class StateTransitionTests {

        @Test
        @DisplayName("start() sollte Status auf ACTIVE setzen, wenn Status PLANNED ist")
        void shouldStartSprintWhenPlanned() {
            assertEquals(SprintStatus.PLANNED, sprint.getStatus());

            sprint.start();

            assertEquals(SprintStatus.ACTIVE, sprint.getStatus());
            assertTrue(sprint.isActive());
        }

        @Test
        @DisplayName("start() sollte IllegalStateException werfen, wenn Status nicht PLANNED ist")
        void shouldThrowExceptionWhenStartingNonPlannedSprint() {
            sprint.start(); // Status ist jetzt ACTIVE
            assertEquals(SprintStatus.ACTIVE, sprint.getStatus());

            assertThrows(IllegalStateException.class, () -> sprint.start());
        }

        @Test
        @DisplayName("end() sollte Status auf DONE setzen, wenn Status ACTIVE ist")
        void shouldEndSprintWhenActive() {
            sprint.start(); // Status ist ACTIVE
            assertEquals(SprintStatus.ACTIVE, sprint.getStatus());

            sprint.end();

            assertEquals(SprintStatus.DONE, sprint.getStatus());
            assertFalse(sprint.isActive());
        }

        @Test
        @DisplayName("end() sollte IllegalStateException werfen, wenn Status nicht ACTIVE ist")
        void shouldThrowExceptionWhenEndingNonActiveSprint() {
            assertEquals(SprintStatus.PLANNED, sprint.getStatus()); // Status ist PLANNED

            assertThrows(IllegalStateException.class, () -> sprint.end());
        }

        @Test
        @DisplayName("isActive() sollte true/false korrekt basierend auf dem Status zurückgeben")
        void shouldReturnCorrectIsActiveState() {
            assertFalse(sprint.isActive()); // PLANNED

            sprint.start();
            assertTrue(sprint.isActive()); // ACTIVE

            sprint.end();
            assertFalse(sprint.isActive()); // DONE
        }
    }

    @Nested
    @DisplayName("Backlog-Verwaltungstests")
    class BacklogManagementTests {

        @Test
        @DisplayName("addUserStoriesToSprint sollte Backlog-Items hinzufügen")
        void shouldAddUserStoriesToBacklog() {
            List<UserStory> stories = List.of(mockUserStory1, mockUserStory2);
            assertTrue(sprint.getBacklogItems().isEmpty());

            sprint.addUserStoriesToSprint(stories);

            assertEquals(2, sprint.getBacklogItems().size());
        }

        @Test
        @DisplayName("addSprintSummaryItems sollte Summary-Items hinzufügen")
        void shouldAddSprintSummaryItems() {
            List<UserStory> stories = List.of(mockUserStory1, mockUserStory2);
            assertTrue(sprint.getSprintSummaryItems().isEmpty());

            sprint.addSprintSummaryItems(stories);

            assertEquals(2, sprint.getSprintSummaryItems().size());
        }

        @Test
        @DisplayName("end() sollte unvollständige Backlog-Items entfernen und detachFromSprint() aufrufen")
        void shouldHandleUncompletedItemsOnEnd() {
            // Arrange
            SprintBacklogItem completedItem = mock(SprintBacklogItem.class);
            SprintBacklogItem uncompletedItem = mock(SprintBacklogItem.class);

            when(completedItem.isCompleted()).thenReturn(true);
            when(uncompletedItem.isCompleted()).thenReturn(false);

            sprint.getBacklogItems().add(completedItem);
            sprint.getBacklogItems().add(uncompletedItem);
            assertEquals(2, sprint.getBacklogItems().size());

            // Act
            sprint.start(); // Muss ACTIVE sein zum Beenden
            sprint.end();

            // Assert
            assertEquals(1, sprint.getBacklogItems().size());
            assertTrue(sprint.getBacklogItems().contains(completedItem));
            assertFalse(sprint.getBacklogItems().contains(uncompletedItem));
            verify(uncompletedItem).detachFromSprint(); // Wichtiger Seiteneffekt
            verify(completedItem, never()).detachFromSprint();
        }
    }

    @Nested
    @DisplayName("Story-Interaktionstests (im aktiven Sprint)")
    class StoryInteractionTests {

        @BeforeEach
        void setupActiveSprint() {
            sprint.start();
            when(mockBacklogItem.getUserStory()).thenReturn(mockUserStory1);
        }

        @Test
        @DisplayName("updateBacklogItemStatus (DONE) sollte UserStory-Status aktualisieren und Summary 'complete' markieren")
        void shouldUpdateStatusToDoneAndCompleteSummary() {
            sprint.updateBacklogItemStatus(mockBacklogItem, StoryStatus.DONE, mockSummaryItem);

            verify(mockUserStory1).updateStatus(StoryStatus.DONE);
            verify(mockSummaryItem).complete();
            verify(mockSummaryItem, never()).notComplete();
        }

        @Test
        @DisplayName("updateBacklogItemStatus (IN_PROGRESS) sollte UserStory-Status aktualisieren und Summary 'notComplete' markieren")
        void shouldUpdateStatusToInProgressAndNotCompleteSummary() {
            sprint.updateBacklogItemStatus(mockBacklogItem, StoryStatus.IN_PROGRESS, mockSummaryItem);

            verify(mockUserStory1).updateStatus(StoryStatus.IN_PROGRESS);
            verify(mockSummaryItem).notComplete();
            verify(mockSummaryItem, never()).complete();
        }

        @Test
        @DisplayName("assignMemberToStory sollte Zuweisung an BacklogItem delegieren")
        void shouldDelegateAssignMember() {
            sprint.assignMemberToStory(mockBacklogItem, mockMember);
            verify(mockBacklogItem).assignMember(mockMember);
        }

        @Test
        @DisplayName("unassignMemberFromStory sollte Zuweisungsaufhebung an BacklogItem delegieren")
        void shouldDelegateUnassignMember() {
            sprint.unassignMemberFromStory(mockBacklogItem, mockMember);
            verify(mockBacklogItem).unassignMember(mockMember);
        }

        @Test
        @DisplayName("addCommentToStory sollte Kommenterstellung an BacklogItem delegieren")
        void shouldDelegateAddComment() {
            sprint.addCommentToStory(mockBacklogItem, mockMember, mockCommentRequest);
            verify(mockBacklogItem).addComment(mockMember, mockCommentRequest);
        }

        @Test
        @DisplayName("addBlockerToStory sollte Blocker-Erstellung an BacklogItem delegieren")
        void shouldDelegateAddBlocker() {
            sprint.addBlockerToStory(mockBacklogItem, mockMember, mockCommentRequest);
            verify(mockBacklogItem).addBlocker(mockMember, mockCommentRequest);
        }

        @Test
        @DisplayName("removeCommentFromStory sollte Entfernung an BacklogItem delegieren")
        void shouldDelegateRemoveComment() {
            sprint.removeCommentFromStory(mockBacklogItem, mockComment);
            verify(mockBacklogItem).removeComment(mockComment);
        }

        @Test
        @DisplayName("Interaktionen sollten SprintNotActiveException werfen, wenn Sprint nicht ACTIVE ist")
        void shouldThrowExceptionWhenNotActive() {
            // Arrange
            // Erstelle einen neuen Sprint, der garantiert NICHT aktiv ist (PLANNED)
            Sprint plannedSprint = Sprint.builder()
                    .status(SprintStatus.PLANNED)
                    .id(99L)
                    .build();

            // Act & Assert
            assertThrows(SprintNotActiveException.class, () ->
                    plannedSprint.updateBacklogItemStatus(mockBacklogItem, StoryStatus.DONE, mockSummaryItem));

            assertThrows(SprintNotActiveException.class, () ->
                    plannedSprint.assignMemberToStory(mockBacklogItem, mockMember));

            assertThrows(SprintNotActiveException.class, () ->
                    plannedSprint.unassignMemberFromStory(mockBacklogItem, mockMember));

            assertThrows(SprintNotActiveException.class, () ->
                    plannedSprint.addCommentToStory(mockBacklogItem, mockMember, mockCommentRequest));

            assertThrows(SprintNotActiveException.class, () ->
                    plannedSprint.addBlockerToStory(mockBacklogItem, mockMember, mockCommentRequest));

            assertThrows(SprintNotActiveException.class, () ->
                    plannedSprint.removeCommentFromStory(mockBacklogItem, mockComment));
        }
    }
}