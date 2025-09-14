package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.TaskResponseDto;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(source = "basicDetails.id", target = "projectId")
    @Mapping(source = "assignedMembers", target = "assignedToMemberUsernames")
    TaskResponseDto toResponse(Task task);

    default String map(ProjectMember member) {
        if (member == null || member.getUser() == null) {
            return null;
        }
        return member.getUser().getUsername();
    }
}
