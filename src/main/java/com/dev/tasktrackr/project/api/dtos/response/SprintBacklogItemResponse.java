package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.scrum.StoryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SprintBacklogItemResponse {
    private Long id; // sprintBacklogItemId
    private String title;
    private String description;
    private int storyPoints;
    private Priority priority;
    private StoryStatus status;
    private List<CommentResponseDto> blockers = new ArrayList<>();
    private List<CommentResponseDto> comments = new ArrayList<>();
    private List<String> assignees = new ArrayList<>();
}
