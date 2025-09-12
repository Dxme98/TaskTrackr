package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.domain.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "role", source = "projectRole.name")
    @Mapping(target = "permissions", source = "projectRole.permissions")
    ProjectMemberDto toResponse(ProjectMember projectMember);
}
