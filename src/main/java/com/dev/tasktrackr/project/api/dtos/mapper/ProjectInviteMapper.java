package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectInviteMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderUsername", source = "sender.username") // UserEntity.username
    @Mapping(target = "receiverUsername", source = "receiver.username")
    @Mapping(target = "receiverId", source = "receiver.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ProjectInviteResponseDto toResponse(ProjectInvite invite);
}
