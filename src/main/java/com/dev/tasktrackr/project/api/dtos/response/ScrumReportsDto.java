package com.dev.tasktrackr.project.api.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScrumReportsDto {
    private ActiveSprintData activeSprintData;
    private List<ScrumMemberStatisticDto> scrumMemberStatisticDtos;
    private ScrumProjectStatisticsDto scrumProjectStatisticsDto;
}
