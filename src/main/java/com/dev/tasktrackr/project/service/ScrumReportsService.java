package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.response.ActiveSprintData;
import com.dev.tasktrackr.project.api.dtos.response.ScrumMemberStatisticDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumProjectStatisticsDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumReportsDto;
import com.dev.tasktrackr.project.repository.ScrumReportRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class ScrumReportsService {
    private final ScrumReportRepository scrumReportRepository;
    private final ProjectAccessService projectAccessService;

    @Transactional(readOnly = true)
    public ScrumReportsDto getScrumReport(Long projectId, String jwtUserId) {
        projectAccessService.checkProjectMemberShip(projectId, jwtUserId);

        // Calculate stats
        ActiveSprintData activeSprintData = scrumReportRepository.getActiveSprintData(projectId).orElse(ActiveSprintData.builder().build());
        List<ScrumMemberStatisticDto> memberStatisticDtoList = scrumReportRepository.getScrumMemberStatisticsForActiveSprint(projectId);
        ScrumProjectStatisticsDto scrumProjectStatisticsDto = getScrumProjectStatisticsDto(projectId);

        return ScrumReportsDto.builder()
                .activeSprintData(activeSprintData)
                .scrumProjectStatisticsDto(scrumProjectStatisticsDto)
                .scrumMemberStatisticDtos(memberStatisticDtoList)
                .build();
    }

    /** HELPER */
    ScrumProjectStatisticsDto getScrumProjectStatisticsDto(Long projectId ) {
        return scrumReportRepository.getScrumProjectStatisticsDto(projectId)
                .orElse(new ScrumProjectStatisticsDto());
    }

}
