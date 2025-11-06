package com.dev.tasktrackr.ProjectTests.domain.scrum;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.scrumdetails.domain.ScrumDetails;
import com.dev.tasktrackr.scrumdetails.domain.Sprint;
import com.dev.tasktrackr.scrumdetails.domain.SprintSummaryItem;
import com.dev.tasktrackr.scrumdetails.domain.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("SprintSummaryItem Entität Tests")
public class SprintSummaryItemTest {

    @Mock
    private UserStory mockUserStory;

    @Mock
    private Sprint mockSprint;

    @Mock
    private ScrumDetails mockScrumDetails;

    @Mock
    private Project mockProject;

    private SprintSummaryItem item;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        item = SprintSummaryItem.builder()
                .id(1L)
                .project(mockProject)
                .sprint(mockSprint)
                .userStory(mockUserStory)
                .title("Bestehender Titel")
                .storyPoints(5)
                .isCompleted(false)
                .build();
    }

    @Nested
    @DisplayName("Erstellungstests")
    class CreationTests {

        @Test
        @DisplayName("Sollte SprintSummaryItem mit Factory-Methode 'create' erstellen")
        void shouldCreateSprintSummaryItemWithFactoryMethod() {
            // Arrange
            when(mockUserStory.getTitle()).thenReturn("Neue User Story");
            when(mockUserStory.getStoryPoints()).thenReturn(8);
            when(mockSprint.getScrumDetails()).thenReturn(mockScrumDetails);
            when(mockScrumDetails.getProject()).thenReturn(mockProject);

            // Act
            SprintSummaryItem newItem = SprintSummaryItem.create(mockUserStory, mockSprint);

            // Assert
            assertNotNull(newItem);
            assertNull(newItem.getId()); // ID ist vor Persistierung null
            assertEquals(mockProject, newItem.getProject());
            assertEquals(mockSprint, newItem.getSprint());
            assertEquals(mockUserStory, newItem.getUserStory());
            assertEquals("Neue User Story", newItem.getTitle());
            assertEquals(8, newItem.getStoryPoints());
            assertFalse(newItem.isCompleted()); // Standardwert muss false sein
        }
    }

    @Nested
    @DisplayName("Zustandsänderungstests")
    class StateModificationTests {

        @Test
        @DisplayName("complete() sollte isCompleted auf true setzen")
        void shouldSetCompletedToTrue() {
            // Arrange
            assertFalse(item.isCompleted()); // Ausgangszustand

            // Act
            item.complete();

            // Assert
            assertTrue(item.isCompleted());
        }

        @Test
        @DisplayName("notComplete() sollte isCompleted auf false setzen (von true)")
        void shouldSetCompletedToFalseFromTrue() {
            // Arrange
            item.complete(); // Setze auf true
            assertTrue(item.isCompleted());

            // Act
            item.notComplete();

            // Assert
            assertFalse(item.isCompleted());
        }

        @Test
        @DisplayName("notComplete() sollte isCompleted auf false belassen (von false)")
        void shouldKeepCompletedFalseFromFalse() {
            // Arrange
            assertFalse(item.isCompleted()); // Bereits false

            // Act
            item.notComplete();

            // Assert
            assertFalse(item.isCompleted());
        }
    }
}