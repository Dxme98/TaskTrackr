package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.ProjectRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "permissions", source = "permissions")
    ProjectRoleResponse toResponse(ProjectRole projectRole);
}
