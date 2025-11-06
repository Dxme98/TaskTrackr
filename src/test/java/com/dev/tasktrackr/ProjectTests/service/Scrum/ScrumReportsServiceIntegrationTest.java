package com.dev.tasktrackr.ProjectTests.service.Scrum;

import com.dev.tasktrackr.ProjectTests.service.shared.ScrumBaseTest;
import com.dev.tasktrackr.scrumdetails.api.dtos.response.ScrumMemberStatisticDto;
import com.dev.tasktrackr.scrumdetails.api.dtos.response.ScrumReportsDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.enums.ProjectType;
import com.dev.tasktrackr.scrumdetails.service.ScrumReportsService;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ScrumReportsService Integration Tests")
public class ScrumReportsServiceIntegrationTest extends ScrumBaseTest {

    @Autowired
    private ScrumReportsService scrumReportsService;

    private UserEntity testUser;
    private UserEntity anotherUser;
    private Project scrumProject;

    @BeforeEach
    void setUp() {
        testUser = testDataFactory.createTestUser("reportUser123", "reportUser");
        anotherUser = testDataFactory.createTestUser("outsider987", "outsiderUser");
        scrumProject = testDataFactory.createTestProject(
                "Scrum Report Project",
                ProjectType.SCRUM,
                testUser
        );
    }

    @Nested
    @DisplayName("Report Access and Empty State Tests")
    class ReportAccessAndEmptyStateTests {

        @Test
        @DisplayName("Sollte Exception werfen, wenn Benutzer kein Projektmitglied ist")
        void shouldThrowExceptionIfUserIsNotProjectMember() {
            Long projectId = scrumProject.getId();
            String nonMemberId = anotherUser.getId();

            // When & Then
            assertThrows(UserNotProjectMemberException.class,
                    () -> scrumReportsService.getScrumReport(projectId, nonMemberId)
            );
        }

        @Test
        @DisplayName("Sollte validen Report für ein neues Projekt ohne Sprints zurückgeben")
        void shouldReturnDefaultReportForProjectWithNoSprintData() {
            // When
            ScrumReportsDto report = scrumReportsService.getScrumReport(scrumProject.getId(), testUser.getId());

            // Then
            assertThat(report).isNotNull();

            // 1. Prüfe ActiveSprintData (sollte leer/default sein)
            assertThat(report.getActiveSprintData()).isNotNull();
            assertThat(report.getActiveSprintData().getTotalStories()).isNull();
            assertThat(report.getActiveSprintData().getFinishedStories()).isNull();
            assertThat(report.getActiveSprintData().getTotalPoints()).isNull();
            assertThat(report.getActiveSprintData().getFinishedPoints()).isNull();
            assertThat(report.getActiveSprintData().getDaysLeft()).isZero();
            assertThat(report.getActiveSprintData().getAverageDailyVelocity()).isZero();

            // 2. Prüfe ScrumMemberStatisticDtos
            assertThat(report.getScrumMemberStatisticDtos()).isNotNull().hasSize(1);

            ScrumMemberStatisticDto memberStats = report.getScrumMemberStatisticDtos().get(0);
            assertThat(memberStats.getUsername()).isEqualTo(testUser.getUsername()); // "reportUser"
            assertThat(memberStats.getTotalTasks()).isZero();
            assertThat(memberStats.getTotalPoints()).isZero();
            assertThat(memberStats.getFinishedPoints()).isZero();
            assertThat(memberStats.getTotalBlocker()).isZero();
            assertThat(memberStats.getTasksInBacklog()).isZero();
            assertThat(memberStats.getTasksInProgress()).isZero();
            assertThat(memberStats.getTasksInReview()).isZero();
            assertThat(memberStats.getTasksInDone()).isZero();
            assertThat(memberStats.getDoneTasksPercentage()).isZero();

            // 3. Prüfe ScrumProjectStatisticsDto
            assertThat(report.getScrumProjectStatisticsDto()).isNotNull();
            assertThat(report.getScrumProjectStatisticsDto().getFinishedSprints()).isEqualTo(0L);
            assertThat(report.getScrumProjectStatisticsDto().getTotalCompletedPoints()).isEqualTo(0L);
            assertThat(report.getScrumProjectStatisticsDto().getAverageVelocity()).isEqualTo(0L);
        }
    }
}
