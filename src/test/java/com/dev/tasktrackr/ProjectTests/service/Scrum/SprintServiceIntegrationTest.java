package com.dev.tasktrackr.ProjectTests.service.Scrum;

import com.dev.tasktrackr.ProjectTests.service.shared.ScrumBaseTest;
import com.dev.tasktrackr.scrumdetails.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.scrumdetails.api.dtos.response.SprintResponseDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.scrumdetails.repository.SprintBacklogItemRepository;
import com.dev.tasktrackr.scrumdetails.repository.SprintRepository;
import com.dev.tasktrackr.scrumdetails.repository.UserStoryRepository;
import com.dev.tasktrackr.scrumdetails.service.SprintService;
import com.dev.tasktrackr.scrumdetails.domain.*;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.ActiveSprintAlreadyExistsException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.NoActiveSprintFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.SprintNotFoundException;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SprintService Integration Tests")
public class SprintServiceIntegrationTest extends ScrumBaseTest {

    @Autowired
    private SprintService sprintService;

    @Autowired
    private SprintRepository sprintRepository;


    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private SprintBacklogItemRepository sprintBacklogItemRepository;

    private UserEntity testUser; // Owner
    private UserEntity anotherUser; // Non-Member
    private Project scrumProject;
    private ScrumDetails scrumDetails;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createTestUser("sprintUser123", "sprintUser");
        anotherUser = testDataFactory.createTestUser("outsiderSprint", "outsider");
        scrumProject = testDataFactory.createTestProject("Scrum Project Sprint", ProjectType.SCRUM, testUser);
        scrumDetails = scrumProject.getScrumDetails();
    }

    @Nested
    @DisplayName("Create Sprint Tests")
    class CreateSprintTests {

        private UserStory story1;
        private UserStory story2;
        private CreateSprintRequest validRequest;

        @BeforeEach
        void setUpStoriesAndRequest() {
            // Given: User Stories, die noch keinem Sprint zugewiesen sind
            story1 = testDataFactory.createTestUserStory("Story 1 for Sprint", 5, StoryStatus.NOT_ASSIGNED_TO_SPRINT, scrumDetails);
            story2 = testDataFactory.createTestUserStory("Story 2 for Sprint", 8, StoryStatus.NOT_ASSIGNED_TO_SPRINT, scrumDetails);

            validRequest = new CreateSprintRequest(
                    "Sprint Alpha",
                    "Initiales Release",
                    "Beschreibung...",
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(15),
                    Set.of(story1.getId(), story2.getId())
            );
        }

        @Test
        @DisplayName("Sollte Sprint erfolgreich erstellen und User Stories zuweisen")
        void shouldCreateSprintSuccessfully() {
            // When
            SprintResponseDto result = sprintService.createSprint(validRequest, scrumProject.getId(), testUser.getId());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Sprint Alpha");
            assertThat(result.getGoal()).isEqualTo("Initiales Release");
            assertThat(result.getStatus()).isEqualTo(SprintStatus.PLANNED);
            assertThat(result.getTotalStories()).isEqualTo(2);
            assertThat(result.getTotalStoryPoints()).isEqualTo(13); // 5 + 8
            assertThat(result.getCompletedStories()).isZero();
            assertThat(result.getSprintSummaryItems()).hasSize(2);
            assertThat(result.getSprintSummaryItems().stream()
                    .map(item -> item.getTitle())
                    .collect(Collectors.toSet()))
                    .containsExactlyInAnyOrder("Story 1 for Sprint", "Story 2 for Sprint");

            // Verify DB: SprintBacklogItems und SprintSummaryItems
            Sprint savedSprint = sprintRepository.findById(result.getId()).orElseThrow();
            assertThat(savedSprint.getBacklogItems()).hasSize(2);
            assertThat(savedSprint.getSprintSummaryItems()).hasSize(2);

            // Verify DB: UserStory Status
            UserStory updatedStory1 = userStoryRepository.findById(story1.getId()).orElseThrow();
            assertThat(updatedStory1.getStatus()).isEqualTo(StoryStatus.SPRINT_BACKLOG);
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer keine Berechtigung hat (canPlanSprint)")
        void shouldThrowIfUserLacksPermission() {
            // Given: Ein User mit der BASE-Rolle
            UserEntity baseMemberUser = testDataFactory.createTestUser("baseSprintUser", "baseSprint");
            testDataFactory.createTestMember(scrumProject, baseMemberUser);

            // When/Then
            assertThrows(PermissionDeniedException.class,
                    () -> sprintService.createSprint(validRequest, scrumProject.getId(), baseMemberUser.getId())
            );
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer kein Projektmitglied ist")
        void shouldThrowIfUserIsNotMember() {
            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> sprintService.createSprint(validRequest, scrumProject.getId(), anotherUser.getId())
            );
        }
    }

    @Nested
    @DisplayName("Start Sprint Tests")
    class StartSprintTests {

        private Sprint plannedSprint;

        @BeforeEach
        void setUpPlannedSprint() {
            plannedSprint = testDataFactory.createTestSprint("Planned Sprint", SprintStatus.PLANNED, scrumDetails);
        }

        @Test
        @DisplayName("Sollte Sprint erfolgreich starten")
        void shouldStartSprintSuccessfully() {
            // When
            SprintResponseDto result = sprintService.startSprint(plannedSprint.getId(), scrumProject.getId(), testUser.getId());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(plannedSprint.getId());
            assertThat(result.getStatus()).isEqualTo(SprintStatus.ACTIVE);

            // Verify DB
            Sprint savedSprint = sprintRepository.findById(result.getId()).orElseThrow();
            assertThat(savedSprint.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Sprint nicht existiert")
        void shouldThrowIfSprintNotFound() {
            // When/Then
            assertThrows(SprintNotFoundException.class,
                    () -> sprintService.startSprint(999L, scrumProject.getId(), testUser.getId())
            );
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn bereits ein Sprint aktiv ist")
        void shouldThrowIfAnotherSprintIsActive() {
            // Given: Ein bereits aktiver Sprint
            testDataFactory.createTestSprint("Active Sprint", SprintStatus.ACTIVE, scrumDetails);

            // When/Then
            assertThrows(ActiveSprintAlreadyExistsException.class,
                    () -> sprintService.startSprint(plannedSprint.getId(), scrumProject.getId(), testUser.getId())
            );
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer keine Berechtigung hat (canStartSprint)")
        void shouldThrowIfUserLacksPermission() {
            // Given: Ein User mit der BASE-Rolle
            UserEntity baseMemberUser = testDataFactory.createTestUser("baseStartUser", "baseStart");
            testDataFactory.createTestMember(scrumProject, baseMemberUser);

            // When/Then
            assertThrows(PermissionDeniedException.class,
                    () -> sprintService.startSprint(plannedSprint.getId(), scrumProject.getId(), baseMemberUser.getId())
            );
        }
    }

    @Nested
    @DisplayName("End Sprint Tests")
    class EndSprintTests {

        private Sprint activeSprint;
        private UserStory storyInProgress;
        private UserStory storyDone;
        private SprintBacklogItem backlogItemInProgress;
        private SprintBacklogItem backlogItemDone;

        @BeforeEach
        void setUpActiveSprint() {
            // Given: Ein aktiver Sprint
            activeSprint = testDataFactory.createTestSprint("Active Sprint to End", SprintStatus.ACTIVE, scrumDetails);

            // Given: Stories mit unterschiedlichem Status
            storyInProgress = testDataFactory.createTestUserStory("In Progress Story", 5, StoryStatus.IN_PROGRESS, scrumDetails);
            storyDone = testDataFactory.createTestUserStory("Done Story", 8, StoryStatus.DONE, scrumDetails);

            // Given: Backlog Items, die dem Sprint zugeordnet sind
            backlogItemInProgress = sprintBacklogItemRepository.save(
                    SprintBacklogItem.create(storyInProgress, activeSprint)
            );
            backlogItemDone = sprintBacklogItemRepository.save(
                    SprintBacklogItem.create(storyDone, activeSprint)
            );
        }

        @Test
        @DisplayName("Sollte Sprint erfolgreich beenden")
        void shouldEndSprintSuccessfully() {
            // When
            SprintResponseDto result = sprintService.endSprint(activeSprint.getId(), scrumProject.getId(), testUser.getId());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(SprintStatus.DONE);

            // Verify DB
            Sprint savedSprint = sprintRepository.findById(result.getId()).orElseThrow();
            assertThat(savedSprint.getStatus()).isEqualTo(SprintStatus.DONE);
        }
    }

    @Nested
    @DisplayName("Find Sprint Tests")
    class FindSprintTests {

        private Sprint activeSprint;
        private Sprint plannedSprint;
        private Sprint doneSprint;

        @BeforeEach
        void setUpSprints() {
            // Verwende die TestDataFactory
            activeSprint = testDataFactory.createTestSprint("Active Sprint", SprintStatus.ACTIVE, scrumDetails);
            plannedSprint = testDataFactory.createTestSprint("Planned Sprint", SprintStatus.PLANNED, scrumDetails);
            doneSprint = testDataFactory.createTestSprint("Done Sprint", SprintStatus.DONE, scrumDetails);
        }

        @Test
        @DisplayName("Sollte aktiven Sprint finden")
        void shouldFindActiveSprint() {
            // When
            SprintResponseDto result = sprintService.findActiveSprint(scrumProject.getId(), testUser.getId());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(activeSprint.getId());
            assertThat(result.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        }

        @Test
        @DisplayName("Sollte NoActiveSprintFoundException werfen, wenn kein Sprint aktiv ist")
        void shouldThrowWhenNoActiveSprintFound() {
            // Given: Lösche den aktiven Sprint
            sprintRepository.delete(activeSprint);

            // When/Then
            assertThrows(NoActiveSprintFoundException.class,
                    () -> sprintService.findActiveSprint(scrumProject.getId(), testUser.getId())
            );
        }

        @Test
        @DisplayName("Sollte alle Sprints nach Status 'PLANNED' finden")
        void shouldFindAllSprintsByStatusPlanned() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<SprintResponseDto> result = sprintService.findAllSprintsByProjectIdAndStatus(
                    scrumProject.getId(), testUser.getId(), pageable, SprintStatus.PLANNED
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(plannedSprint.getId());
        }

        @Test
        @DisplayName("Sollte leere Seite für nicht vorhandenen Status zurückgeben")
        void shouldReturnEmptyPageForStatusWithNoSprints() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            // Erstelle ein neues Projekt ohne Sprints
            Project projectWithoutSprints = testDataFactory.createTestProject("No Sprints", ProjectType.SCRUM, testUser);

            // When
            Page<SprintResponseDto> result = sprintService.findAllSprintsByProjectIdAndStatus(
                    projectWithoutSprints.getId(), testUser.getId(), pageable, SprintStatus.DONE
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer (findActive) kein Mitglied ist")
        void shouldThrowIfNotMemberForFindActive() {
            // When/Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> sprintService.findActiveSprint(scrumProject.getId(), anotherUser.getId())
            );
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer (findAllByStatus) kein Mitglied ist")
        void shouldThrowIfNotMemberForFindAllByStatus() {
            // When/Then
            Pageable pageable = PageRequest.of(0, 10);
            assertThrows(UserNotProjectMemberException.class,
                    () -> sprintService.findAllSprintsByProjectIdAndStatus(
                            scrumProject.getId(), anotherUser.getId(), pageable, SprintStatus.DONE
                    )
            );
        }
    }
}