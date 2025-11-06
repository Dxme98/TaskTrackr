package com.dev.tasktrackr.basicdetails.api.dtos.mapper;

import com.dev.tasktrackr.basicdetails.api.dtos.response.CommentResponseDto;
import com.dev.tasktrackr.scrumdetails.domain.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "sprintBacklogItemId", source = "sprintBacklogItem.id")
    @Mapping(target = "createdByUsername", source = "createdBy.user.username")
    CommentResponseDto toResponse(Comment comment);
}
