package com.dev.tasktrackr.ProjectTests.domain.scrum;

import com.dev.tasktrackr.scrumdetails.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.scrumdetails.domain.*;
import com.dev.tasktrackr.shared.exception.custom.BadRequestExceptions.InvalidMemberAssignmentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SprintBacklogItem Entität Tests")
public class SprintBacklogItemTest {

    @Mock
    private Sprint mockSprint;

    @Mock
    private UserStory mockUserStory;

    @Mock
    private ProjectMember mockMember;

    @Mock
    private ProjectMember mockMember2;

    @Mock
    private CreateCommentRequest mockCommentRequest;

    @Mock
    private Comment mockComment;

    private SprintBacklogItem sprintBacklogItem;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        sprintBacklogItem = SprintBacklogItem.builder()
                .id(1L)
                .userStory(mockUserStory)
                .sprint(mockSprint)
                .assignedMembers(new HashSet<>())
                .comments(new HashSet<>())
                .build();
    }

    @Nested
    @DisplayName("Erstellungstests")
    class CreationTests {

        @Test
        @DisplayName("Sollte SprintBacklogItem erstellen und UserStory-Status aktualisieren")
        void shouldCreateItemAndUpdateUserStoryStatus() {
            // Act
            SprintBacklogItem item = SprintBacklogItem.create(mockUserStory, mockSprint);

            // Assert
            assertNotNull(item);
            assertEquals(mockUserStory, item.getUserStory());
            assertEquals(mockSprint, item.getSprint());
            assertTrue(item.getAssignedMembers().isEmpty());
            assertTrue(item.getComments().isEmpty());

            // Überprüfen des wichtigen Seiteneffekts
            verify(mockUserStory).updateStatus(StoryStatus.SPRINT_BACKLOG);
        }
    }

    @Nested
    @DisplayName("Zuweisungstests (Assign/Unassign)")
    class AssignmentTests {

        @Test
        @DisplayName("assignMember sollte Mitglied hinzufügen, wenn Status SPRINT_BACKLOG ist")
        void shouldAssignMemberWhenStatusIsSprintBacklog() {
            // Arrange
            when(mockUserStory.getStatus()).thenReturn(StoryStatus.SPRINT_BACKLOG);
            assertTrue(sprintBacklogItem.getAssignedMembers().isEmpty());

            // Act
            sprintBacklogItem.assignMember(mockMember);

            // Assert
            assertEquals(1, sprintBacklogItem.getAssignedMembers().size());
            assertTrue(sprintBacklogItem.getAssignedMembers().contains(mockMember));
        }

        @Test
        @DisplayName("assignMember sollte Exception werfen, wenn Status nicht SPRINT_BACKLOG ist")
        void shouldThrowExceptionWhenAssigningIfStatusNotSprintBacklog() {
            // Arrange
            when(mockUserStory.getStatus()).thenReturn(StoryStatus.IN_PROGRESS); // Falscher Status

            // Act & Assert
            assertThrows(InvalidMemberAssignmentException.class, () -> {
                sprintBacklogItem.assignMember(mockMember);
            });
            assertTrue(sprintBacklogItem.getAssignedMembers().isEmpty());
        }

        @Test
        @DisplayName("unassignMember sollte Mitglied entfernen, wenn Status SPRINT_BACKLOG ist")
        void shouldUnassignMemberWhenStatusIsSprintBacklog() {
            // Arrange
            when(mockUserStory.getStatus()).thenReturn(StoryStatus.SPRINT_BACKLOG);
            sprintBacklogItem.getAssignedMembers().add(mockMember); // Vorher hinzufügen
            assertEquals(1, sprintBacklogItem.getAssignedMembers().size());

            // Act
            sprintBacklogItem.unassignMember(mockMember);

            // Assert
            assertTrue(sprintBacklogItem.getAssignedMembers().isEmpty());
        }

        @Test
        @DisplayName("unassignMember sollte Exception werfen, wenn Status nicht SPRINT_BACKLOG ist")
        void shouldThrowExceptionWhenUnassigningIfStatusNotSprintBacklog() {
            // Arrange
            when(mockUserStory.getStatus()).thenReturn(StoryStatus.DONE); // Falscher Status
            sprintBacklogItem.getAssignedMembers().add(mockMember);

            // Act & Assert
            assertThrows(InvalidMemberAssignmentException.class, () -> {
                sprintBacklogItem.unassignMember(mockMember);
            });
            assertEquals(1, sprintBacklogItem.getAssignedMembers().size()); // Mitglied wurde nicht entfernt
        }
    }

    @Nested
    @DisplayName("Kommentar-Tests")
    class CommentTests {

        @Test
        @DisplayName("addComment sollte einen Standard-Kommentar hinzufügen")
        void shouldAddComment() {
            // Arrange
            when(mockCommentRequest.getMessage()).thenReturn("Testkommentar");
            assertTrue(sprintBacklogItem.getComments().isEmpty());

            // Act
            Comment addedComment = sprintBacklogItem.addComment(mockMember, mockCommentRequest);

            // Assert
            assertEquals(1, sprintBacklogItem.getComments().size());
            assertTrue(sprintBacklogItem.getComments().contains(addedComment));
            assertEquals(CommentType.COMMENT, addedComment.getType());
            assertEquals(mockMember, addedComment.getCreatedBy());
        }

        @Test
        @DisplayName("addBlocker sollte einen Blocker-Kommentar hinzufügen")
        void shouldAddBlocker() {
            // Arrange
            when(mockCommentRequest.getMessage()).thenReturn("Blocker!");
            assertTrue(sprintBacklogItem.getComments().isEmpty());

            // Act
            Comment addedBlocker = sprintBacklogItem.addBlocker(mockMember, mockCommentRequest);

            // Assert
            assertEquals(1, sprintBacklogItem.getComments().size());
            assertTrue(sprintBacklogItem.getComments().contains(addedBlocker));
            assertEquals(CommentType.BLOCKER, addedBlocker.getType());
            assertEquals(mockMember, addedBlocker.getCreatedBy());
        }

        @Test
        @DisplayName("removeComment sollte einen Kommentar entfernen")
        void shouldRemoveComment() {
            // Arrange
            // Wir verwenden ein echtes, wenn auch gemocktes Objekt, das dem Set hinzugefügt wird
            sprintBacklogItem.getComments().add(mockComment);
            assertEquals(1, sprintBacklogItem.getComments().size());

            // Act
            Comment removedComment = sprintBacklogItem.removeComment(mockComment);

            // Assert
            assertTrue(sprintBacklogItem.getComments().isEmpty());
            assertEquals(mockComment, removedComment);
        }
    }

    @Nested
    @DisplayName("Zustandsüberprüfungstests")
    class StateValidationTests {

        @Test
        @DisplayName("detachFromSprint sollte userStory.detachBacklogItem() aufrufen")
        void shouldDelegateDetachFromSprint() {
            // Act
            sprintBacklogItem.detachFromSprint();

            // Assert
            verify(mockUserStory).detachBacklogItem();
        }

        @Test
        @DisplayName("isCompleted sollte true zurückgeben, wenn UserStory-Status DONE ist")
        void shouldReturnTrueWhenCompleted() {
            // Arrange
            when(mockUserStory.getStatus()).thenReturn(StoryStatus.DONE);

            // Act & Assert
            assertTrue(sprintBacklogItem.isCompleted());
        }

        @Test
        @DisplayName("isCompleted sollte false zurückgeben, wenn UserStory-Status nicht DONE ist")
        void shouldReturnFalseWhenNotCompleted() {
            // Arrange
            when(mockUserStory.getStatus()).thenReturn(StoryStatus.IN_PROGRESS);

            // Act & Assert
            assertFalse(sprintBacklogItem.isCompleted());
        }

        @Test
        @DisplayName("memberIsAssigned sollte true zurückgeben, wenn Mitglied zugewiesen ist")
        void shouldReturnTrueIfMemberIsAssigned() {
            // Arrange
            sprintBacklogItem.getAssignedMembers().add(mockMember);

            // Act & Assert
            assertTrue(sprintBacklogItem.memberIsAssigned(mockMember));
        }

        @Test
        @DisplayName("memberIsAssigned sollte false zurückgeben, wenn Mitglied nicht zugewiesen ist")
        void shouldReturnFalseIfMemberIsNotAssigned() {
            // Arrange
            sprintBacklogItem.getAssignedMembers().add(mockMember2); // Anderes Mitglied

            // Act & Assert
            assertFalse(sprintBacklogItem.memberIsAssigned(mockMember));
            assertTrue(sprintBacklogItem.getAssignedMembers().contains(mockMember2)); // Sicherstellen, dass das Set nicht leer ist
        }
    }
}