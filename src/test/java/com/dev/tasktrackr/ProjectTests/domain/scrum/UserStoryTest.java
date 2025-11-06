package com.dev.tasktrackr.ProjectTests.domain.scrum;

import com.dev.tasktrackr.scrumdetails.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.scrumdetails.domain.ScrumDetails;
import com.dev.tasktrackr.scrumdetails.domain.SprintBacklogItem;
import com.dev.tasktrackr.scrumdetails.domain.StoryStatus;
import com.dev.tasktrackr.scrumdetails.domain.UserStory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("UserStory Entität Tests")
public class UserStoryTest {

    @Mock
    private ScrumDetails mockScrumDetails;

    @Mock
    private CreateUserStoryRequest mockCreateRequest;

    @Mock
    private SprintBacklogItem mockSprintBacklogItem;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("Erstellungstests")
    class CreationTests {

        @Test
        @DisplayName("Sollte UserStory mit Factory-Methode 'create' erstellen")
        void shouldCreateUserStoryWithFactoryMethod() {
            // Arrange
            when(mockCreateRequest.getTitle()).thenReturn("Als User möchte ich...");
            when(mockCreateRequest.getDescription()).thenReturn("Beschreibung der Story.");
            when(mockCreateRequest.getPriority()).thenReturn(Priority.HIGH);
            when(mockCreateRequest.getStoryPoints()).thenReturn(5);

            // Act
            UserStory story = UserStory.create(mockCreateRequest, mockScrumDetails);

            // Assert
            assertNotNull(story);
            assertEquals("Als User möchte ich...", story.getTitle());
            assertEquals("Beschreibung der Story.", story.getDescription());
            assertEquals(Priority.HIGH, story.getPriority());
            assertEquals(5, story.getStoryPoints());
            assertEquals(mockScrumDetails, story.getScrumDetails());
            assertEquals(StoryStatus.NOT_ASSIGNED_TO_SPRINT, story.getStatus());
            assertNull(story.getId()); // ID ist vor Persistierung null
            assertNull(story.getSprintBacklogItem());
        }
    }

    @Nested
    @DisplayName("Zustandsänderungstests")
    class StateModificationTests {

        private UserStory story;

        @BeforeEach
        void setupStory() {
            // Erstelle eine UserStory im "aktiven" Zustand (zugewiesen)
            story = UserStory.builder()
                    .id(1L)
                    .title("Bestehende Story")
                    .status(StoryStatus.IN_PROGRESS) // Angenommen, sie ist in Arbeit
                    .sprintBacklogItem(mockSprintBacklogItem) // Verbunden mit einem BacklogItem
                    .build();
        }

        @Test
        @DisplayName("detachBacklogItem() sollte Status zurücksetzen und Item entfernen")
        void shouldDetachBacklogItemAndResetStatus() {
            // Arrange
            assertEquals(StoryStatus.IN_PROGRESS, story.getStatus());
            assertNotNull(story.getSprintBacklogItem());

            // Act
            story.detachBacklogItem();

            // Assert
            assertNull(story.getSprintBacklogItem());
            assertEquals(StoryStatus.NOT_ASSIGNED_TO_SPRINT, story.getStatus());
        }

        @Test
        @DisplayName("updateStatus() sollte den Status korrekt aktualisieren")
        void shouldUpdateStatus() {
            // Arrange
            assertEquals(StoryStatus.IN_PROGRESS, story.getStatus());

            // Act
            story.updateStatus(StoryStatus.DONE);

            // Assert
            assertEquals(StoryStatus.DONE, story.getStatus());
        }

        @Test
        @DisplayName("updateStatus() sollte Status auf einen anderen Wert aktualisieren")
        void shouldUpdateStatusToAnotherValue() {
            // Arrange
            assertEquals(StoryStatus.IN_PROGRESS, story.getStatus());

            // Act
            story.updateStatus(StoryStatus.REVIEW);

            // Assert
            assertEquals(StoryStatus.REVIEW, story.getStatus());
        }
    }
}