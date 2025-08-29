package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.project.api.dtos.response.ProjectDetailsBasicDto;
import com.dev.tasktrackr.project.api.dtos.response.ProjectDetailsScrumDto;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.user.UserDto;
import com.dev.tasktrackr.user.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface ProjectMapper {


    @Mapping(target = "id", source = "id")
    ProjectOverviewDto toOverviewDto(Project project);
    ProjectDetailsBasicDto toBasicDetailsDto(Project project);
    ProjectDetailsScrumDto toScrumDetailsDto(Project project);


    UserDto toUserDto(UserEntity userEntity);

    @Mapping(target = "user", source = "user")
    ProjectMemberDto toProjectMemberDto(ProjectMember projectMember);
}
