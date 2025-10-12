package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.scrum.SprintBacklogItem;
import com.dev.tasktrackr.project.domain.scrum.SprintStatus;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class SprintResponseDto {
    private Long id;
    private String name;
    private String goal;
    private String description;
    private SprintStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalStories;
    private int completedStories;
    private int totalStoryPoints;
    private int completedStoryPoints;
    private double progressPercentage;
    private Set<SprintBacklogItemResponse> sprintBacklogItems = new HashSet<>();
}
