package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.ProjectRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "projectId", source = "project.id")
    ProjectRoleResponse toResponse(ProjectRole projectRole);
}
