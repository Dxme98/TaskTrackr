package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data // Lombok für Getter, Setter, etc.
@NoArgsConstructor
@AllArgsConstructor
public class BoardUserStoryDto {
    private Long id;
    private String title;
    private String description;
    private int storyPoints;
    private Priority priority;
    private String status;
    private List<CommentDto> blockers = new ArrayList<>();
    private List<CommentDto> comments = new ArrayList<>();
    private List<String> assignees = new ArrayList<>();
}
