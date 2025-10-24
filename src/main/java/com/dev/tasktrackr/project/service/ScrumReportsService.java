package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.response.ActiveSprintData;
import com.dev.tasktrackr.project.api.dtos.response.ScrumMemberStatisticDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumProjectStatisticsDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumReportsDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.scrum.ScrumDetails;
import com.dev.tasktrackr.project.repository.ScrumReportRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.NoActiveSprintFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrumReportsService {
    private final ScrumReportRepository scrumReportRepository;
    private final ProjectAccessService projectAccessService;

    @Transactional(readOnly = true)
    public ScrumReportsDto getScrumReport(Long projectId, String jwtUserId) {
        projectAccessService.checkProjectMemberShip(projectId, jwtUserId);

        // Load data
        Project project = projectAccessService.findProjectById(projectId);
        ScrumDetails scrumDetails = project.getScrumDetails();
        ActiveSprintData activeSprintData = scrumReportRepository.getActiveSprintData(projectId).orElseThrow(
                ()-> new NoActiveSprintFoundException(projectId));

        List<ScrumMemberStatisticDto> memberStatisticDtoList = scrumDetails.getMemberStatisticsList();
        ScrumProjectStatisticsDto scrumProjectStatisticsDto = scrumDetails.getProjectStatistics();


        return ScrumReportsDto.builder()
                .activeSprintData(activeSprintData)
                .scrumProjectStatisticsDto(scrumProjectStatisticsDto)
                .scrumMemberStatisticDtos(memberStatisticDtoList)
                .build();
    }

}
