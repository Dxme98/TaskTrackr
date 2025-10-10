package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserStoryMapper {
    UserStoryResponseDto toDto(UserStory userStory);
}
