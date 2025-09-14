package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberOverviewDto;
import com.dev.tasktrackr.project.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "basicDetails.id", target = "projectId")
    @Mapping(source = "assignedMembers", target = "assignedToMembers")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "updatedBy", target = "updatedBy")
    TaskResponseDto toResponse(Task task);

    default ProjectMemberOverviewDto map(ProjectMember member) {
        if (member == null || member.getUser() == null) {
            return null;
        }
        return new ProjectMemberOverviewDto(
                member.getId(),
                member.getUser().getUsername()
        );
    }
}
