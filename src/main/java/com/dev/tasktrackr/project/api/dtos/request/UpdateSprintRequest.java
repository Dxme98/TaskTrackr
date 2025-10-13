package com.dev.tasktrackr.project.api.dtos.request;

import com.dev.tasktrackr.project.domain.scrum.SprintStatus;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateSprintRequest {
    private String name;
    private String goal;
    private String description;
    private SprintStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<Long> userStoryIds = new HashSet<>();
}
