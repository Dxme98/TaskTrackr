package com.dev.tasktrackr.project.api.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SprintBacklogItemResponse {
    private Long id;
    private Long sprintId;
    private String userStoryTitle;
    private Set<ProjectMemberDto> assignedMembers = new HashSet<>();
    private Set<CommentResponseDto> comments;
}
