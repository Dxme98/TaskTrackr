package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectType;
import com.dev.tasktrackr.user.UserDto;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    // Hauptmapping für Project -> ProjectDto.Response
    @Mapping(target = "id", source = "id.value")
    ProjectDto.Response toResponse(Project project);

    // User Mapping
    @Mapping(target = "id", source = "id.value")
    UserDto toUserDto(UserEntity user);

    // ProjectType Mapping
    ProjectTypeDto toProjectTypeDto(ProjectType projectType);

    // Liste von ProjectMembers
   // List<ProjectMemberDto> toProjectMemberDtos(Set<ProjectMember> projectMembers);
}