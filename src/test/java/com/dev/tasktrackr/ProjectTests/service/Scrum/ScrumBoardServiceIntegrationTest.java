package com.dev.tasktrackr.ProjectTests.service.Scrum;

import com.dev.tasktrackr.ProjectTests.service.shared.ScrumBaseTest;
import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.response.ScrumBoardResponseDto;
import com.dev.tasktrackr.project.api.dtos.response.SprintBacklogItemResponse;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.project.domain.scrum.*;
import com.dev.tasktrackr.project.repository.*;
import com.dev.tasktrackr.project.service.ScrumBoardService;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.PermissionDeniedException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.NoActiveSprintFoundException;
import com.dev.tasktrackr.user.domain.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ScrumBoardService Integration Tests")
public class ScrumBoardServiceIntegrationTest extends ScrumBaseTest {

    @Autowired
    private ScrumBoardService scrumBoardService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private SprintBacklogItemRepository sprintBacklogItemRepository;

    @Autowired
    private SprintSummaryItemRepository sprintSummaryItemRepository;

    @Autowired
    private CommentRepository commentRepository;

    // --- Benutzer ---
    private UserEntity projectOwnerUser;
    private UserEntity anotherMemberUser;
    private UserEntity outsiderUser;

    // --- Projekt & Sprint ---
    private Project scrumProject;
    private ScrumDetails scrumDetails;
    private Sprint activeSprint;

    // --- Mitglieder ---
    private ProjectMember projectOwner;
    private ProjectMember anotherMember;

    // --- Sprint-Artefakte ---
    private UserStory storyTodo;
    private UserStory storyInProgress;
    private SprintBacklogItem backlogItemTodo;
    private SprintBacklogItem backlogItemInProgress;
    private SprintSummaryItem summaryItemTodo;
    private SprintSummaryItem summaryItemInProgress;


    @BeforeEach
    void setUp() {
        projectOwnerUser = testDataFactory.createTestUser("boardOwner1", "boardOwner");
        anotherMemberUser = testDataFactory.createTestUser("boardMember2", "boardMember");
        outsiderUser = testDataFactory.createTestUser("boardOutsider3", "boardOutsider");

        scrumProject = testDataFactory.createTestProject("Scrum Board Project", ProjectType.SCRUM, projectOwnerUser);
        scrumDetails = scrumProject.getScrumDetails();

        projectOwner = projectMemberRepository.findProjectMemberByUserIdAndProjectId(projectOwnerUser.getId(), scrumProject.getId())
                .orElseThrow(() -> new IllegalStateException("Owner-Mitglied wurde nicht korrekt erstellt."));
        anotherMember = testDataFactory.createTestMember(scrumProject, anotherMemberUser); // Hat BASE-Rolle

        activeSprint = testDataFactory.createTestSprint("Active Board Sprint", SprintStatus.ACTIVE, scrumDetails);

        storyTodo = testDataFactory.createTestUserStory("Story TODO", 5, StoryStatus.NOT_ASSIGNED_TO_SPRINT, scrumDetails);
        storyInProgress = testDataFactory.createTestUserStory("Story IN_PROGRESS", 8, StoryStatus.NOT_ASSIGNED_TO_SPRINT, scrumDetails);

        backlogItemTodo = SprintBacklogItem.create(storyTodo, activeSprint);
        summaryItemTodo = SprintSummaryItem.create(storyTodo, activeSprint);

        backlogItemInProgress = SprintBacklogItem.create(storyInProgress, activeSprint);
        summaryItemInProgress = SprintSummaryItem.create(storyInProgress, activeSprint);

        sprintBacklogItemRepository.saveAll(List.of(backlogItemTodo, backlogItemInProgress));
        sprintSummaryItemRepository.saveAll(List.of(summaryItemTodo, summaryItemInProgress));

        storyInProgress.updateStatus(StoryStatus.IN_PROGRESS);
        userStoryRepository.save(storyInProgress);


        entityManager.flush();
        entityManager.clear();

        projectOwner = projectMemberRepository.findById(projectOwner.getId()).orElseThrow();
        anotherMember = projectMemberRepository.findById(anotherMember.getId()).orElseThrow();
        backlogItemTodo = sprintBacklogItemRepository.findById(backlogItemTodo.getId()).orElseThrow();
        backlogItemInProgress = sprintBacklogItemRepository.findById(backlogItemInProgress.getId()).orElseThrow();
    }

    @Nested
    @DisplayName("Get Scrum Board Tests")
    class GetScrumBoardTests {

        @Test
        @DisplayName("Sollte Scrum Board mit korrekt sortierten Items zurückgeben")
        void shouldGetScrumBoardSuccessfully() {
            // When
            ScrumBoardResponseDto board = scrumBoardService.getScrumBoard(scrumProject.getId(), projectOwnerUser.getId());

            // Then
            assertThat(board).isNotNull();
            assertThat(board.getSprintName()).isEqualTo(activeSprint.getName());
            assertThat(board.getProjectMembers()).hasSize(2);

            assertThat(board.getTodo()).hasSize(1);
            assertThat(board.getTodo().get(0).getId()).isEqualTo(backlogItemTodo.getId());
            assertThat(board.getInProgress()).hasSize(1);
            assertThat(board.getInProgress().get(0).getId()).isEqualTo(backlogItemInProgress.getId());

            assertThat(board.getTodo().get(0).getComments()).isEmpty();
            assertThat(board.getTodo().get(0).getBlockers()).isEmpty();
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn kein Sprint aktiv ist")
        void shouldThrowIfNoActiveSprint() {
            // Given
            UserEntity user = testDataFactory.createTestUser("noSprintUser", "noSprint");
            Project projectWithoutSprint = testDataFactory.createTestProject("No Sprint Project", ProjectType.SCRUM, user);

            // When / Then
            assertThrows(NoActiveSprintFoundException.class,
                    () -> scrumBoardService.getScrumBoard(projectWithoutSprint.getId(), user.getId())
            );
        }

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer kein Mitglied ist")
        void shouldThrowIfUserNotMember() {
            // When / Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> scrumBoardService.getScrumBoard(scrumProject.getId(), outsiderUser.getId())
            );
        }
    }

    @Nested
    @DisplayName("Update User Story Status Tests")
    class UpdateUserStoryStatusTests {

        @Test
        @DisplayName("Sollte Status erfolgreich auf IN_PROGRESS aktualisieren")
        void shouldUpdateUserStoryStatusSuccessfully() {
            // When
            SprintBacklogItemResponse response = scrumBoardService.updateUserStoryStatus(
                    scrumProject.getId(), backlogItemTodo.getId(), StoryStatus.IN_PROGRESS, projectOwnerUser.getId()
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(StoryStatus.IN_PROGRESS);

            // Verify DB;
            UserStory updatedStory = userStoryRepository.findById(storyTodo.getId()).orElseThrow();
            assertThat(updatedStory.getStatus()).isEqualTo(StoryStatus.IN_PROGRESS);

            SprintSummaryItem updatedSummary = sprintSummaryItemRepository.findById(summaryItemTodo.getId()).orElseThrow();
            assertThat(updatedSummary.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("Sollte Status auf DONE aktualisieren und SummaryItem als 'completed' markieren")
        void shouldUpdateStatusToDoneAndMarkSummaryItemComplete() {
            // When
            SprintBacklogItemResponse response = scrumBoardService.updateUserStoryStatus(
                    scrumProject.getId(), backlogItemInProgress.getId(), StoryStatus.DONE, projectOwnerUser.getId()
            );

            // Then
            assertThat(response.getStatus()).isEqualTo(StoryStatus.DONE);

            // Verify DB
            UserStory updatedStory = userStoryRepository.findById(storyInProgress.getId()).orElseThrow();
            assertThat(updatedStory.getStatus()).isEqualTo(StoryStatus.DONE);

            SprintSummaryItem updatedSummary = sprintSummaryItemRepository.findById(summaryItemInProgress.getId()).orElseThrow();
            assertThat(updatedSummary.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("Sollte Status-Update erlauben, wenn Benutzer zugewiesen ist (ohne Hauptberechtigung)")
        void shouldAllowUpdateIfUserIsAssigned() {
            // Wir müssen das backlogItemTodo neu laden, da es 'detached' ist
            SprintBacklogItem item = sprintBacklogItemRepository.findById(backlogItemTodo.getId()).orElseThrow();
            item.assignMember(anotherMember);
            sprintBacklogItemRepository.saveAndFlush(item);

            // When
            SprintBacklogItemResponse response = scrumBoardService.updateUserStoryStatus(
                    scrumProject.getId(), backlogItemTodo.getId(), StoryStatus.IN_PROGRESS, anotherMemberUser.getId()
            );

            // Then
            assertThat(response.getStatus()).isEqualTo(StoryStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Sollte PermissionDeniedException werfen (nicht zugewiesen + keine Berechtigung)")
        void shouldThrowPermissionDeniedIfNotAssignedAndNoPermission() {
            // When / Then
            assertThrows(PermissionDeniedException.class,
                    () -> scrumBoardService.updateUserStoryStatus(
                            scrumProject.getId(), backlogItemInProgress.getId(), StoryStatus.REVIEW, anotherMemberUser.getId()
                    )
            );
        }
    }

    @Nested
    @DisplayName("Assign/Unassign Member Tests")
    class AssignMemberTests {

        @Test
        @DisplayName("Sollte Benutzer erfolgreich einer Story zuweisen")
        void shouldAssignMemberToStory() {
            // When
            SprintBacklogItemResponse response = scrumBoardService.assignMemberToStory(
                    scrumProject.getId(), backlogItemTodo.getId(), anotherMember.getId(), projectOwnerUser.getId()
            );

            // Then
            assertThat(response.getAssignees()).hasSize(1);
            assertThat(response.getAssignees().get(0)).isEqualTo(anotherMemberUser.getUsername());

            // Verify DB
            SprintBacklogItem updatedItem = sprintBacklogItemRepository.findSprintBacklogItemById(backlogItemTodo.getId()).orElseThrow();
            assertThat(updatedItem.getAssignedMembers().stream().map(ProjectMember::getId))
                    .contains(anotherMember.getId());
        }

        @Test
        @DisplayName("Sollte Zuweisung von Benutzer erfolgreich entfernen")
        void shouldUnassignMemberFromStory() {
            // Given: Benutzer zuerst zuweisen
            scrumBoardService.assignMemberToStory(
                    scrumProject.getId(), backlogItemTodo.getId(), anotherMember.getId(), projectOwnerUser.getId()
            );

            // When
            SprintBacklogItemResponse response = scrumBoardService.unassignMemberFromStory(
                    scrumProject.getId(), backlogItemTodo.getId(), anotherMember.getId(), projectOwnerUser.getId()
            );

            // Then
            assertThat(response.getAssignees()).isEmpty();

            // Verify DB
            SprintBacklogItem updatedItem = sprintBacklogItemRepository.findSprintBacklogItemById(backlogItemTodo.getId()).orElseThrow();
            assertThat(updatedItem.getAssignedMembers()).isEmpty();
        }

        @Test
        @DisplayName("Sollte PermissionDeniedException werfen (canAssignUserToStory)")
        void shouldThrowPermissionDeniedForAssign() {
            // When / Then
            assertThrows(PermissionDeniedException.class,
                    () -> scrumBoardService.assignMemberToStory(
                            scrumProject.getId(), backlogItemTodo.getId(), anotherMember.getId(), anotherMemberUser.getId()
                    )
            );
        }
    }

    @Nested
    @DisplayName("Comment and Blocker Tests")
    class CommentAndBlockerTests {

        private CreateCommentRequest commentRequest;
        private CreateCommentRequest blockerRequest;

        @BeforeEach
        void setUpRequests() {
            commentRequest = new CreateCommentRequest("Dies ist ein neuer Kommentar.");
            blockerRequest = new CreateCommentRequest("Dies ist ein neuer Blocker!");

            // Lade die Entitäten neu, die in den Tests verwendet werden
            projectOwner = projectMemberRepository.findById(projectOwner.getId()).orElseThrow();
            anotherMember = projectMemberRepository.findById(anotherMember.getId()).orElseThrow();
            backlogItemTodo = sprintBacklogItemRepository.findById(backlogItemTodo.getId()).orElseThrow();
        }

        @Test
        @DisplayName("Sollte Kommentar erfolgreich hinzufügen")
        void shouldAddCommentToStory() {
            // Given: Der Sprint startet ohne Kommentare (verifiziert im Setup)

            // When
            SprintBacklogItemResponse response = scrumBoardService.addCommentToStory(
                    scrumProject.getId(), backlogItemTodo.getId(), commentRequest, projectOwnerUser.getId()
            );

            // Then
            // (0 Setup-Kommentare + 1 neuer Kommentar)
            assertThat(response.getComments()).hasSize(1);
            // (0 Setup-Blocker)
            assertThat(response.getBlockers()).isEmpty();
            assertThat(response.getComments().get(0).getMessage()).isEqualTo("Dies ist ein neuer Kommentar.");
        }

        @Test
        @DisplayName("Sollte Blocker erfolgreich hinzufügen")
        void shouldAddBlockerToStory() {
            // Given: Der Sprint startet ohne Blocker

            // When
            SprintBacklogItemResponse response = scrumBoardService.addBlockerToStory(
                    scrumProject.getId(), backlogItemTodo.getId(), blockerRequest, projectOwnerUser.getId()
            );

            // Then
            // (0 Setup-Kommentare)
            assertThat(response.getComments()).isEmpty();
            // (0 Setup-Blocker + 1 neuer Blocker)
            assertThat(response.getBlockers()).hasSize(1);
            assertThat(response.getBlockers().get(0).getMessage()).isEqualTo("Dies ist ein neuer Blocker!");
        }

        @Test
        @DisplayName("Sollte Kommentar erfolgreich entfernen")
        void shouldRemoveCommentFromStory() {
            // Given: Erstelle den Kommentar, der gelöscht werden soll
            Comment commentToRemove = Comment.createComment(projectOwner, new CreateCommentRequest("Bitte löschen (Kommentar)"), backlogItemTodo);
            commentRepository.save(commentToRemove);
            long commentId = commentToRemove.getId();

            assertTrue(commentRepository.findById(commentId).isPresent());

            // When
            scrumBoardService.removeCommentFromStory(
                    scrumProject.getId(), backlogItemTodo.getId(), commentId, projectOwnerUser.getId()
            );

            // Verify DB
            entityManager.flush();
            assertThat(commentRepository.findById(commentId)).isEmpty();
        }

        @Test
        @DisplayName("Sollte Blocker erfolgreich entfernen")
        void shouldRemoveBlockerFromStory() {
            // Given: Erstelle den Blocker, der gelöscht werden soll
            Comment blockerToRemove = Comment.createBlocker(projectOwner, new CreateCommentRequest("Bitte löschen (Blocker)"), backlogItemTodo);
            commentRepository.save(blockerToRemove);
            long blockerId = blockerToRemove.getId();

            assertTrue(commentRepository.findById(blockerId).isPresent());

            // When
            scrumBoardService.removeBlockerFromStory(
                    scrumProject.getId(), backlogItemTodo.getId(), blockerId, projectOwnerUser.getId()
            );

            // Verify DB
            entityManager.flush();
            assertThat(commentRepository.findById(blockerId)).isEmpty();
        }

        @Test
        @DisplayName("Sollte PermissionDeniedException beim Entfernen von Kommentaren werfen")
        void shouldThrowPermissionDeniedForRemoveComment() {
            // Given: Erstelle einen Kommentar zum Löschen
            Comment commentToRemove = Comment.createComment(projectOwner, new CreateCommentRequest("Ein Kommentar"), backlogItemTodo);
            commentRepository.save(commentToRemove);

            // When / Then
            assertThrows(PermissionDeniedException.class,
                    () -> scrumBoardService.removeCommentFromStory(
                            scrumProject.getId(), backlogItemTodo.getId(), commentToRemove.getId(), anotherMemberUser.getId()
                    )
            );
        }
    }
}