package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.SprintBacklogItemResponse;
import com.dev.tasktrackr.project.domain.scrum.CommentType;
import com.dev.tasktrackr.project.domain.scrum.SprintBacklogItem;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SprintBacklogItemMapper {
    private final CommentMapper commentMapper;


    public SprintBacklogItemResponse toResponse(SprintBacklogItem sprintBacklogItem) {
        if (sprintBacklogItem == null || sprintBacklogItem.getUserStory() == null) {
            return null;
        }

        UserStory userStory = sprintBacklogItem.getUserStory();
        SprintBacklogItemResponse response = new SprintBacklogItemResponse();

        // 1. Felder direkt aus dem SprintBacklogItem und der UserStory mappen
        response.setId(sprintBacklogItem.getId());
        response.setTitle(userStory.getTitle());
        response.setDescription(userStory.getDescription());
        response.setStoryPoints(userStory.getStoryPoints());
        response.setPriority(userStory.getPriority());
        response.setStatus(userStory.getStatus());

        // 2. Zugewiesene Mitglieder in eine Liste von Namen umwandeln
        if (sprintBacklogItem.getAssignedMembers() != null) {
            response.setAssignees(sprintBacklogItem.getAssignedMembers().stream()
                    .map(member -> member.getUser().getUsername())
                    .collect(Collectors.toList()));
        }

        // 3. Kommentare mit dem injizierten CommentMapper umwandeln und aufteilen
        if (sprintBacklogItem.getComments() != null) {
            response.setBlockers(sprintBacklogItem.getComments().stream()
                    .filter(comment -> comment.getType() == CommentType.BLOCKER)
                    .map(commentMapper::toResponse) // Verwendung des MapStruct-Mappers
                    .collect(Collectors.toList()));

            response.setComments(sprintBacklogItem.getComments().stream()
                    .filter(comment -> comment.getType() == CommentType.COMMENT)
                    .map(commentMapper::toResponse) // Verwendung des MapStruct-Mappers
                    .collect(Collectors.toList()));
        }

        return response;
    }
}

