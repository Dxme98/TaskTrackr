package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import org.springframework.stereotype.Component;

@Component
public class UserStoryMapper {

    public UserStoryResponseDto toDto(UserStory userStory) {
        if (userStory == null) {
            return null;
        }

        UserStoryResponseDto dto = new UserStoryResponseDto();

        dto.setId(userStory.getId());
        dto.setTitle(userStory.getTitle());
        dto.setDescription(userStory.getDescription());
        dto.setPriority(userStory.getPriority());
        dto.setStoryPoints(userStory.getStoryPoints());
        dto.setCreatedAt(userStory.getCreatedAt());
        dto.setStatus(userStory.getStatus());

        if (userStory.getSprintBacklogItem() != null && userStory.getSprintBacklogItem().getSprint() != null) {
            dto.setSprintName(userStory.getSprintBacklogItem().getSprint().getName());
        } else {
            dto.setSprintName("Keinem Sprint zugewiesen");
        }

        return dto;
    }
}
