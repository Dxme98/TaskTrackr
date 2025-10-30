package com.dev.tasktrackr.ProjectTests.service.Scrum;

import com.dev.tasktrackr.ProjectTests.service.shared.ScrumBaseTest;
import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.scrum.ScrumDetails;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import com.dev.tasktrackr.project.repository.UserStoryRepository;
import com.dev.tasktrackr.project.service.UserStoryService;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.UserStoryTitleAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.UserStoryNotFoundException;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserStoryService Integration Tests")
public class UserStoryServiceIntegrationTest extends ScrumBaseTest {

    @Autowired
    private UserStoryService userStoryService;

    @Autowired
    private UserStoryRepository userStoryRepository;

    private UserEntity testUser; // Wird Owner des Projekts
    private UserEntity anotherUser; // Ist kein Mitglied
    private Project scrumProject;
    private ProjectMember projectOwner;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createTestUser("storyUser123", "storyUser");
        anotherUser = testDataFactory.createTestUser("otherUser456", "otherUser");

        scrumProject = testDataFactory.createTestProject("Scrum Project", ProjectType.SCRUM, testUser);

        projectOwner = scrumProject.getProjectMembers().stream()
                .filter(m -> m.getUser().getId().equals(testUser.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Owner wurde im Setup nicht gefunden"));
    }

    @Nested
    @DisplayName("Create User Story Tests")
    class CreateUserStoryTests {

        private CreateUserStoryRequest validRequest;

        @BeforeEach
        void setUpRequest() {
            validRequest = new CreateUserStoryRequest(
                    "Test UserStoryTitle",
                    "Test UserStoryDescription",
                    Priority.HIGH,
                    5
            );
        }

        @Test
        @DisplayName("Sollte User Story erfolgreich erstellen")
        void shouldCreateUserStorySuccessfully() {

            // When
            UserStoryResponseDto result = userStoryService.createUserStory(
                    scrumProject.getId(),
                    validRequest,
                    testUser.getId()
            );

            // Then
            assertNotNull(result);
            assertEquals(validRequest.getTitle(), result.getTitle());
            assertEquals(validRequest.getPriority(), result.getPriority());
            assertEquals(validRequest.getStoryPoints(), result.getStoryPoints());
            assertEquals(StoryStatus.NOT_ASSIGNED_TO_SPRINT, result.getStatus());
            assertEquals("Keinem Sprint zugewiesen", result.getSprintName());
            assertNotNull(result.getCreatedAt());

            // Verifiziere in der Datenbank
            UserStory savedStory = userStoryRepository.findById(result.getId()).orElseThrow();
            assertEquals(validRequest.getTitle(), savedStory.getTitle());
            assertEquals(scrumProject.getId(), savedStory.getScrumDetails().getId());
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Titel bereits existiert")
        void shouldThrowForDuplicateTitle() {
            userStoryService.createUserStory(scrumProject.getId(), validRequest, testUser.getId());

            // When/Then: Versuche, sie erneut zu erstellen
            assertThrows(UserStoryTitleAlreadyExistsException.class,
                    () -> userStoryService.createUserStory(scrumProject.getId(), validRequest, testUser.getId())
            );
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer kein Projektmitglied ist")
        void shouldThrowIfUserIsNotProjectMember() {
            // Given: anotherUser ist kein Mitglied

            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> userStoryService.createUserStory(scrumProject.getId(), validRequest, anotherUser.getId())
            );
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer keine Berechtigung hat")
        void shouldThrowIfUserLacksPermission() {
            // Given: Erstelle einen neuen User und füge ihn mit der BASE-Rolle hinzu
            UserEntity memberUser = testDataFactory.createTestUser("memberUser", "member");
            testDataFactory.createTestMember(scrumProject, memberUser); // Nutzt standardmäßig die BASE-Rolle

            // When/Then: Annahme, dass BASE-Rolle keine SCRUM_CREATE_USER_STORY Berechtigung hat
            assertThrows(PermissionDeniedException.class,
                    () -> userStoryService.createUserStory(scrumProject.getId(), validRequest, memberUser.getId())
            );
        }
    }

    @Nested
    @DisplayName("Delete User Story Tests")
    class DeleteUserStoryTests {

        private UserStory storyToDelete;

        @BeforeEach
        void setUpStory() {
            // Da die TestDataFactory keine createTestUserStory-Methode hat,
            // erstellen wir sie hier manuell über das Repository.
            storyToDelete = UserStory.builder()
                    .title("Story zum Löschen")
                    .priority(Priority.LOW)
                    .storyPoints(3)
                    .status(StoryStatus.NOT_ASSIGNED_TO_SPRINT)
                    .scrumDetails(scrumProject.getScrumDetails())
                    .build();
            storyToDelete = userStoryRepository.save(storyToDelete);
        }

        @Test
        @DisplayName("Sollte User Story erfolgreich löschen")
        void shouldDeleteUserStorySuccessfully() {
            // Given
            long storyId = storyToDelete.getId();
            assertTrue(userStoryRepository.findById(storyId).isPresent());

            // When
            userStoryService.deleteUserStory(scrumProject.getId(), storyId, testUser.getId());

            // Then
            assertFalse(userStoryRepository.findById(storyId).isPresent());
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn User Story nicht existiert")
        void shouldThrowIfUserStoryDoesNotExist() {
            // Given
            long nonExistentStoryId = 9999L;

            // When/Then
            assertThrows(UserStoryNotFoundException.class,
                    () -> userStoryService.deleteUserStory(scrumProject.getId(), nonExistentStoryId, testUser.getId())
            );
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer kein Projektmitglied ist")
        void shouldThrowIfUserIsNotMember() {
            // Given: anotherUser

            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> userStoryService.deleteUserStory(scrumProject.getId(), storyToDelete.getId(), anotherUser.getId())
            );
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer keine Berechtigung hat")
        void shouldThrowIfUserLacksPermission() {
            // Given: Erstelle einen neuen User mit der BASE-Rolle
            UserEntity memberUser = testDataFactory.createTestUser("memberUserDel", "memberDel");
            testDataFactory.createTestMember(scrumProject, memberUser);

            // When/Then: BASE-ROLLE, hat keine Rechte um Stories zu löschen
            assertThrows(PermissionDeniedException.class,
                    () -> userStoryService.deleteUserStory(scrumProject.getId(), storyToDelete.getId(), memberUser.getId())
            );
        }
    }

    @Nested
    @DisplayName("Get User Stories Tests")
    class GetUserStoriesTests {

        private UserStory story1_notAssigned;
        private UserStory story2_notAssigned;
        private UserStory story3_active;

        @BeforeEach
        void setUpStories() {
            ScrumDetails details = scrumProject.getScrumDetails();

            // Story 1 (Nicht zugewiesen)
            story1_notAssigned = userStoryRepository.save(UserStory.builder()
                    .title("Story 1 - Not Assigned")
                    .priority(Priority.MEDIUM).storyPoints(5).status(StoryStatus.NOT_ASSIGNED_TO_SPRINT)
                    .scrumDetails(details).build());

            // Story 2 (Nicht zugewiesen)
            story2_notAssigned = userStoryRepository.save(UserStory.builder()
                    .title("Story 2 - Also Not Assigned")
                    .priority(Priority.LOW).storyPoints(3).status(StoryStatus.NOT_ASSIGNED_TO_SPRINT)
                    .scrumDetails(details).build());

            // Story 3 (Aktiv in einem Sprint)
            story3_active = userStoryRepository.save(UserStory.builder()
                    .title("Story 3 - In Sprint")
                    .priority(Priority.HIGH).storyPoints(8).status(StoryStatus.SPRINT_BACKLOG) // Jeder Status außer NOT_ASSIGNED
                    .scrumDetails(details).build());
        }

        @Test
        @DisplayName("Sollte alle User Stories zurückgeben, wenn Filter null ist")
        void shouldGetAllUserStoriesWhenFilterIsNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<UserStoryResponseDto> result = userStoryService.getUserStoriesByProjectId(
                    scrumProject.getId(), pageable, testUser.getId(), null
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent())
                    .extracting(UserStoryResponseDto::getId)
                    .containsExactlyInAnyOrder(story1_notAssigned.getId(), story2_notAssigned.getId(), story3_active.getId());
        }

        @Test
        @DisplayName("Sollte nur 'NOT_ASSIGNED' Stories mit entsprechendem Filter zurückgeben")
        void shouldGetNotAssignedStoriesWithFilter() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            String filter = "NOT_ASSIGNED_TO_SPRINT";

            // When
            Page<UserStoryResponseDto> result = userStoryService.getUserStoriesByProjectId(
                    scrumProject.getId(), pageable, testUser.getId(), filter
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(UserStoryResponseDto::getId)
                    .containsExactlyInAnyOrder(story1_notAssigned.getId(), story2_notAssigned.getId());
        }

        @Test
        @DisplayName("Sollte nur 'ACTIVE' Stories mit entsprechendem Filter zurückgeben")
        void shouldGetActiveStoriesWithFilter() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            String filter = "ACTIVE";

            // When
            Page<UserStoryResponseDto> result = userStoryService.getUserStoriesByProjectId(
                    scrumProject.getId(), pageable, testUser.getId(), filter
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent())
                    .extracting(UserStoryResponseDto::getId)
                    .containsExactlyInAnyOrder(story3_active.getId());
        }

        @Test
        @DisplayName("Sollte Fallback (alle Stories) bei unbekanntem Filter zurückgeben")
        void shouldGetAllStoriesForUnknownFilter() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            String filter = "SOME_UNKNOWN_FILTER";

            // When
            Page<UserStoryResponseDto> result = userStoryService.getUserStoriesByProjectId(
                    scrumProject.getId(), pageable, testUser.getId(), filter
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer kein Projektmitglied ist")
        void shouldThrowIfUserIsNotMemberForGet() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> userStoryService.getUserStoriesByProjectId(
                            scrumProject.getId(), pageable, anotherUser.getId(), null
                    )
            );
        }
    }
}