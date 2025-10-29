package com.dev.tasktrackr.ProjectTests.domain;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.InviteIsNotPendingException;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UnauthorizedInviteHandleAcception;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("ProjectInvite Entity Tests")
class ProjectInviteTests {

    @Mock
    private UserEntity mockSender;
    @Mock
    private UserEntity mockReceiver;
    @Mock
    private Project mockProject;

    private ProjectInvite projectInvite;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockSender.getId()).thenReturn("sender123");
        when(mockReceiver.getId()).thenReturn("receiver456");
        when(mockProject.getId()).thenReturn(1L);

        projectInvite = ProjectInvite.createInvite(mockSender, mockReceiver, mockProject);
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create invite with correct properties")
        void shouldCreateInviteWithCorrectProperties() {
            assertEquals(mockSender, projectInvite.getSender());
            assertEquals(mockReceiver, projectInvite.getReceiver());
            assertEquals(mockProject, projectInvite.getProject());
            assertEquals(ProjectInviteStatus.PENDING, projectInvite.getInviteStatus());
        }

        @Test
        @DisplayName("Should create invite without sender (nullable)")
        void shouldCreateInviteWithoutSender() {
            ProjectInvite inviteWithoutSender = ProjectInvite.createInvite(null, mockReceiver, mockProject);

            assertNull(inviteWithoutSender.getSender());
            assertEquals(mockReceiver, inviteWithoutSender.getReceiver());
            assertEquals(mockProject, inviteWithoutSender.getProject());
            assertEquals(ProjectInviteStatus.PENDING, inviteWithoutSender.getInviteStatus());
        }
    }

    @Nested
    @DisplayName("Accept Invite Tests")
    class AcceptInviteTests {

        @Test
        @DisplayName("Should accept invite successfully")
        void shouldAcceptInviteSuccessfully() {
            projectInvite.accept("receiver456");

            assertEquals(ProjectInviteStatus.ACCEPTED, projectInvite.getInviteStatus());
        }

        @Test
        @DisplayName("Should throw exception when wrong user tries to accept")
        void shouldThrowExceptionWhenWrongUserTriesToAccept() {
            assertThrows(UnauthorizedInviteHandleAcception.class,
                    () -> projectInvite.accept("wronguser789"));
        }

        @Test
        @DisplayName("Should throw exception when invite is not pending")
        void shouldThrowExceptionWhenInviteIsNotPending() {
            ReflectionTestUtils.setField(projectInvite, "inviteStatus", ProjectInviteStatus.DECLINED);

            assertThrows(InviteIsNotPendingException.class,
                    () -> projectInvite.accept("receiver456"));
        }


        @Test
        @DisplayName("Should not change status when accept throws exception")
        void shouldNotChangeStatusWhenAcceptThrowsException() {
            ProjectInviteStatus originalStatus = projectInvite.getInviteStatus();

            assertThrows(UnauthorizedInviteHandleAcception.class,
                    () -> projectInvite.accept("wronguser789"));

            assertEquals(originalStatus, projectInvite.getInviteStatus());
        }
    }

    @Nested
    @DisplayName("Decline Invite Tests")
    class DeclineInviteTests {

        @Test
        @DisplayName("Should decline invite successfully")
        void shouldDeclineInviteSuccessfully() {
            projectInvite.decline("receiver456");

            assertEquals(ProjectInviteStatus.DECLINED, projectInvite.getInviteStatus());
        }

        @Test
        @DisplayName("Should throw exception when wrong user tries to decline")
        void shouldThrowExceptionWhenWrongUserTriesToDecline() {
            assertThrows(UnauthorizedInviteHandleAcception.class,
                    () -> projectInvite.decline("wronguser789"));
        }

        @Test
        @DisplayName("Should throw exception when invite is not pending for decline")
        void shouldThrowExceptionWhenInviteIsNotPendingForDecline() {
            ReflectionTestUtils.setField(projectInvite, "inviteStatus", ProjectInviteStatus.ACCEPTED);

            assertThrows(InviteIsNotPendingException.class,
                    () -> projectInvite.decline("receiver456"));
        }



        @Test
        @DisplayName("Should not change status when decline throws exception")
        void shouldNotChangeStatusWhenDeclineThrowsException() {
            ProjectInviteStatus originalStatus = projectInvite.getInviteStatus();

            assertThrows(UnauthorizedInviteHandleAcception.class,
                    () -> projectInvite.decline("wronguser789"));

            assertEquals(originalStatus, projectInvite.getInviteStatus());
        }
    }

    @Nested
    @DisplayName("Validation Tests (Interne Logik)")
    class ValidationTests {

        @Test
        @DisplayName("Should validate response successfully for valid user")
        void shouldValidateResponseSuccessfullyForValidUser() {
            assertDoesNotThrow(() -> projectInvite.validateResponse("receiver456"));
        }

        @Test
        @DisplayName("Should throw exception when JWT user is not receiver")
        void shouldThrowExceptionWhenJwtUserIsNotReceiver() {
            assertThrows(UnauthorizedInviteHandleAcception.class,
                    () -> projectInvite.validateResponse("differentuser123"));
        }

        @Test
        @DisplayName("Should throw exception when invite status is ACCEPTED")
        void shouldThrowExceptionWhenInviteStatusIsAccepted() {
            ReflectionTestUtils.setField(projectInvite, "inviteStatus", ProjectInviteStatus.ACCEPTED);

            assertThrows(InviteIsNotPendingException.class,
                    () -> projectInvite.validateResponse("receiver456"));
        }

        @Test
        @DisplayName("Should throw exception when invite status is DECLINED")
        void shouldThrowExceptionWhenInviteStatusIsDeclined() {
            ReflectionTestUtils.setField(projectInvite, "inviteStatus", ProjectInviteStatus.DECLINED);

            assertThrows(InviteIsNotPendingException.class,
                    () -> projectInvite.validateResponse("receiver456"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null JWT user ID")
        void shouldHandleNullJwtUserId() {
            assertThrows(UnauthorizedInviteHandleAcception.class,
                    () -> projectInvite.validateResponse(null));
        }

        @Test
        @DisplayName("Should handle empty JWT user ID")
        void shouldHandleEmptyJwtUserId() {
            assertThrows(UnauthorizedInviteHandleAcception.class,
                    () -> projectInvite.validateResponse(""));
        }

        @Test
        @DisplayName("Should handle invite with null sender")
        void shouldHandleInviteWithNullSender() {
            ProjectInvite inviteWithoutSender = ProjectInvite.createInvite(null, mockReceiver, mockProject);

            assertDoesNotThrow(() -> inviteWithoutSender.accept("receiver456"));
            assertEquals(ProjectInviteStatus.ACCEPTED, inviteWithoutSender.getInviteStatus());
        }

        @Test
        @DisplayName("Should handle multiple validation calls")
        void shouldHandleMultipleValidationCalls() {
            assertDoesNotThrow(() -> {
                projectInvite.validateResponse("receiver456");
                projectInvite.validateResponse("receiver456");
            });
        }

        @Test
        @DisplayName("Should preserve invite ID through status changes")
        void shouldPreserveInviteIdThroughStatusChanges() {
            Long originalId;
            ReflectionTestUtils.setField(projectInvite, "id", 1L);
            originalId = projectInvite.getId();


            projectInvite.accept("receiver456");
            assertEquals(originalId, projectInvite.getId());
        }
    }
}