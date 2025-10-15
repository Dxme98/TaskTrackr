package com.dev.tasktrackr.project.api.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrumBoardResponseDto {
    private String sprintName;
    private String sprintGoal;
    private String sprintDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalStoryPoints;
    private int completedStoryPoints;
    private List<ProjectMemberDto> projectMembers = new ArrayList<>();

    private List<SprintBacklogItemResponse> todo = new ArrayList<>();
    private List<SprintBacklogItemResponse> inProgress = new ArrayList<>();
    private List<SprintBacklogItemResponse> review = new ArrayList<>();
    private List<SprintBacklogItemResponse> done = new ArrayList<>();
}
