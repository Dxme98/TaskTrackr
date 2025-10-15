package com.dev.tasktrackr.project.api.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private List<BoardUserStoryDto> todo = new ArrayList<>();
    private List<BoardUserStoryDto> inProgress = new ArrayList<>();
    private List<BoardUserStoryDto> review = new ArrayList<>();
    private List<BoardUserStoryDto> done = new ArrayList<>();
}
