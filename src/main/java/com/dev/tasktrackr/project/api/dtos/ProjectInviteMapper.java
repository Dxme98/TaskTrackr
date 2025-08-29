package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectInviteMapper {
    @Mapping(target = "senderUsername", source = "sender.username") // UserEntity.username
    @Mapping(target = "receiverUsername", source = "receiver.username")
    @Mapping(target = "projectName", source = "project.name")
    ProjectInviteResponseDto toResponse(ProjectInvite invite);
}
