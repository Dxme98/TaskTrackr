package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.response.ActiveSprintData;
import com.dev.tasktrackr.project.api.dtos.response.ScrumMemberStatisticDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumProjectStatisticsDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumReportsDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.scrum.ScrumDetails;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrumReportsService {
    private final ProjectRepository projectRepository;

    public ScrumReportsDto getScrumReport(Long projectId, String jwtUserId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
        ScrumDetails scrumDetails = project.getScrumDetails();

        ActiveSprintData activeSprintData = scrumDetails.getActiveSprintData();
        List<ScrumMemberStatisticDto> memberStatisticDtoList = scrumDetails.getMemberStatisticsList();
        ScrumProjectStatisticsDto scrumProjectStatisticsDto = scrumDetails.getProjectStatistics();


        return ScrumReportsDto.builder()
                .activeSprintData(activeSprintData)
                .scrumProjectStatisticsDto(scrumProjectStatisticsDto)
                .scrumMemberStatisticDtos(memberStatisticDtoList)
                .build();
    }

}
