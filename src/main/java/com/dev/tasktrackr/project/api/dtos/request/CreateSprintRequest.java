package com.dev.tasktrackr.project.api.dtos.request;

import com.dev.tasktrackr.project.domain.scrum.SprintStatus;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class CreateSprintRequest {
    private String name;
    private String goal;
    private String description;
    private SprintStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<Long> userStoryIds = new HashSet<>();
}
