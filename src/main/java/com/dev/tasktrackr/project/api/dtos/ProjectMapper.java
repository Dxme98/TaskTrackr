package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectType;
import com.dev.tasktrackr.user.UserDto;
import com.dev.tasktrackr.user.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    // Hauptmapping für Project -> ProjectDto.Response
    @Mapping(target = "projectMembers", source = "projectMembers")
    ProjectDto.Response toResponse(Project project);

    // User Mapping
    UserDto toUserDto(UserEntity user);

    // ProjectType Mapping
    ProjectTypeDto toProjectTypeDto(ProjectType projectType);

    // ProjectMember Mapping
    @Mapping(target = "user", source = "user")
    ProjectMemberDto toProjectMemberDto(ProjectMember projectMember);

    // Liste von ProjectMembers
    List<ProjectMemberDto> toProjectMemberDtos(Set<ProjectMember> projectMembers);
}