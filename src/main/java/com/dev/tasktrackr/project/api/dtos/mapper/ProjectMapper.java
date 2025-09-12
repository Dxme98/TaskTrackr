package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.ProjectDetailsBasicDto;
import com.dev.tasktrackr.project.api.dtos.response.ProjectDetailsScrumDto;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface ProjectMapper {


    @Mapping(target = "id", source = "id")
    ProjectOverviewDto toOverviewDto(Project project);
    ProjectDetailsBasicDto toBasicDetailsDto(Project project);
    ProjectDetailsScrumDto toScrumDetailsDto(Project project);
}
