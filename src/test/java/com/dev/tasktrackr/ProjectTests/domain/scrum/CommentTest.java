package com.dev.tasktrackr.ProjectTests.domain.scrum;

import com.dev.tasktrackr.scrumdetails.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.scrumdetails.domain.Comment;
import com.dev.tasktrackr.scrumdetails.domain.CommentType;
import com.dev.tasktrackr.scrumdetails.domain.SprintBacklogItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("Comment Entität Tests")
public class CommentTest {

    @Mock
    private SprintBacklogItem mockSprintBacklogItem;

    @Mock
    private ProjectMember mockMember;

    @Mock
    private CreateCommentRequest mockCommentRequest;

    private final String testMessage = "Das ist eine Testnachricht.";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockCommentRequest.getMessage()).thenReturn(testMessage);
    }

    @Nested
    @DisplayName("Erstellungstests")
    class CreationTests {

        @Test
        @DisplayName("Sollte einen Standard-Kommentar (Typ COMMENT) erstellen")
        void shouldCreateCommentWithFactoryMethod() {
            Comment comment = Comment.createComment(mockMember, mockCommentRequest, mockSprintBacklogItem);


            assertNotNull(comment);
            assertEquals(mockSprintBacklogItem, comment.getSprintBacklogItem());
            assertEquals(mockMember, comment.getCreatedBy());
            assertEquals(testMessage, comment.getMessage());
            assertEquals(CommentType.COMMENT, comment.getType());
            assertNull(comment.getId()); // ID wird erst beim Speichern von JPA gesetzt
        }

        @Test
        @DisplayName("Sollte einen Blocker (Typ BLOCKER) erstellen")
        void shouldCreateBlockerWithFactoryMethod() {
            Comment blocker = Comment.createBlocker(mockMember, mockCommentRequest, mockSprintBacklogItem);

            assertNotNull(blocker);
            assertEquals(mockSprintBacklogItem, blocker.getSprintBacklogItem());
            assertEquals(mockMember, blocker.getCreatedBy());
            assertEquals(testMessage, blocker.getMessage());
            assertEquals(CommentType.BLOCKER, blocker.getType());
        }
    }

    @Nested
    @DisplayName("Zustandsüberprüfungstests")
    class StateValidationTests {

        @Test
        @DisplayName("isBlocker() sollte true zurückgeben, wenn Typ BLOCKER ist")
        void shouldReturnTrueForIsBlockerWhenTypeIsBlocker() {
            Comment blocker = Comment.createBlocker(mockMember, mockCommentRequest, mockSprintBacklogItem);

            assertTrue(blocker.isBlocker());
        }

        @Test
        @DisplayName("isBlocker() sollte false zurückgeben, wenn Typ COMMENT ist")
        void shouldReturnFalseForIsBlockerWhenTypeIsComment() {
            Comment comment = Comment.createComment(mockMember, mockCommentRequest, mockSprintBacklogItem);

            assertFalse(comment.isBlocker());
        }
    }
}