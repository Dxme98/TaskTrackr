package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectInviteMapper {
    @Mapping(target = "id", source = "id.value") // ProjectInviteId zu Long
    @Mapping(target = "senderId", source = "sender.id.value") // UserEntity.id zu String
    @Mapping(target = "senderUsername", source = "sender.username") // UserEntity.username
    @Mapping(target = "receiverId", source = "receiver.id.value")
    @Mapping(target = "receiverUsername", source = "receiver.username")
    @Mapping(target = "projectId", source = "project.id.value")
    @Mapping(target = "projectName", source = "project.name")
    ProjectInviteResponseDto toResponse(ProjectInvite invite);
}
