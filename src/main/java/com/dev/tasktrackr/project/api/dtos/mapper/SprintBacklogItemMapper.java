package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.CommentResponseDto;
import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.response.SprintBacklogItemResponse;
import com.dev.tasktrackr.project.domain.scrum.SprintBacklogItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SprintBacklogItemMapper {
    ProjectMemberMapper projectMemberMapper;
    CommentMapper commentMapper;

    SprintBacklogItemResponse toDto(SprintBacklogItem sprintBacklogItem) {

        if(sprintBacklogItem == null) {
            return null;
        }

        SprintBacklogItemResponse dto = new SprintBacklogItemResponse();

        // Standard felder
        dto.setId(sprintBacklogItem.getId());
        dto.setSprintId(sprintBacklogItem.getSprint().getId());
        dto.setUserStoryTitle(sprintBacklogItem.getUserStory().getTitle());

        // Member mappen
        Set<ProjectMemberDto> assignedMembers = sprintBacklogItem.getAssignedMembers()
                .stream().map(assignedMember -> projectMemberMapper.toResponse(assignedMember))
                .collect(Collectors.toSet());
        dto.setAssignedMembers(assignedMembers);

        // comments mappen
        Set<CommentResponseDto> commentResponseDtos = sprintBacklogItem.getComments().stream()
                .map(comment -> commentMapper.toResponse(comment))
                .collect(Collectors.toSet());
        dto.setComments(commentResponseDtos);


        return dto;
    }
}
