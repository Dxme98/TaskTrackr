package com.dev.tasktrackr.ProjectTests.controller;

import com.dev.tasktrackr.config.JpaAuditingConfig;
import com.dev.tasktrackr.project.api.controller.ScrumReportsController;
import com.dev.tasktrackr.project.api.dtos.response.ActiveSprintData;
import com.dev.tasktrackr.project.api.dtos.response.ScrumMemberStatisticDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumProjectStatisticsDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumReportsDto;
import com.dev.tasktrackr.project.service.ScrumReportsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ScrumReportsController.class)
@ImportAutoConfiguration(exclude = JpaAuditingConfig.class)
@DisplayName("ScrumReportsController (WebMvcTest)")
class ScrumReportsControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScrumReportsService scrumReportsService;

    private static final String API_BASE_URL_TEMPLATE = "/api/v1/projects/{projectId}/scrum-reports";

    private String testUserId;
    private String testUsername;
    private Long testProjectId;

    @BeforeEach
    void setUp() {
        testUserId = "user-id-123";
        testUsername = "testuser";
        testProjectId = 1L;
    }

    /**
     * Erstellt ein voll funktionsfähiges, gemocktes ScrumReportsDto für Tests.
     */
    private ScrumReportsDto createMockScrumReportDto() {
        // 1. Active Sprint Data
        // (Sprint läuft seit 5 Tagen, 10 Tage verbleibend)
        LocalDate startDate = LocalDate.now().minusDays(4);
        LocalDate endDate = LocalDate.now().plusDays(9);

        // Annahme: 20 von 50 Punkten in 5 Tagen geschafft
        ActiveSprintData activeData = new ActiveSprintData(
                10L, 4L, 50L, 20L, startDate, endDate
        );

        // 2. Member Statistics
        ScrumMemberStatisticDto member1 = new ScrumMemberStatisticDto(
                "user.one", 5L, 25L, 10L, 1L, 1L, 2L, 1L, 1L
        );

        ScrumMemberStatisticDto member2 = new ScrumMemberStatisticDto(
                "user.two", 10L, 25L, 10L, 0L, 0L, 5L, 1L, 4L
        );

        // 3. Project Statistics
        ScrumProjectStatisticsDto projectStats = new ScrumProjectStatisticsDto(
                4L, 200L
        );

        // 4. Gesamt-DTO
        return ScrumReportsDto.builder()
                .activeSprintData(activeData)
                .scrumMemberStatisticDtos(Arrays.asList(member1, member2))
                .scrumProjectStatisticsDto(projectStats)
                .build();
    }

    @Test
    @DisplayName("GET /{projectId}/scrum-reports - Sollte 200 OK und den Report zurückgeben")
    void getScrumReport_whenValidRequest_shouldReturn200AndReport() throws Exception {
        // Given
        ScrumReportsDto mockReport = createMockScrumReportDto();
        when(scrumReportsService.getScrumReport(eq(testProjectId), eq(testUserId)))
                .thenReturn(mockReport);

        // When & Then
        mockMvc.perform(get(API_BASE_URL_TEMPLATE, testProjectId)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", testUserId)
                                .claim("preferred_username", testUsername)
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Überprüfe ActiveSprintData (inkl. berechneter Felder)
                .andExpect(jsonPath("$.activeSprintData").exists())
                .andExpect(jsonPath("$.activeSprintData.totalPoints", is(50)))
                .andExpect(jsonPath("$.activeSprintData.finishedPoints", is(20)))
                .andExpect(jsonPath("$.activeSprintData.daysLeft", is(9))) // Berechnet
                .andExpect(jsonPath("$.activeSprintData.averageDailyVelocity", is(4))) // Berechnet (20/5)

                // Überprüfe ScrumProjectStatisticsDto (inkl. berechneter Felder)
                .andExpect(jsonPath("$.scrumProjectStatisticsDto").exists())
                .andExpect(jsonPath("$.scrumProjectStatisticsDto.finishedSprints", is(4)))
                .andExpect(jsonPath("$.scrumProjectStatisticsDto.totalCompletedPoints", is(200)))
                .andExpect(jsonPath("$.scrumProjectStatisticsDto.averageVelocity", is(50))) // Berechnet (200/4)

                // Überprüfe ScrumMemberStatisticDtos (inkl. berechneter Felder)
                .andExpect(jsonPath("$.scrumMemberStatisticDtos").isArray())
                .andExpect(jsonPath("$.scrumMemberStatisticDtos", hasSize(2)))
                .andExpect(jsonPath("$.scrumMemberStatisticDtos[0].username", is("user.one")))
                .andExpect(jsonPath("$.scrumMemberStatisticDtos[0].totalBlocker", is(1)))
                .andExpect(jsonPath("$.scrumMemberStatisticDtos[0].doneTasksPercentage", is(20))) // Berechnet (1/5)
                .andExpect(jsonPath("$.scrumMemberStatisticDtos[1].username", is("user.two")))
                .andExpect(jsonPath("$.scrumMemberStatisticDtos[1].tasksInDone", is(4)))
                .andExpect(jsonPath("$.scrumMemberStatisticDtos[1].doneTasksPercentage", is(40))); // Berechnet (4/10)

        // Verifiziere Service-Aufruf
        verify(scrumReportsService).getScrumReport(eq(testProjectId), eq(testUserId));
    }
}