package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.CommentResponseDto;
import com.dev.tasktrackr.project.domain.scrum.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "sprintBacklogItemId", source = "sprintBacklogItem.id")
    @Mapping(target = "createdByUsername", source = "createdBy.user.username")
    CommentResponseDto toResponse(Comment comment);
}
