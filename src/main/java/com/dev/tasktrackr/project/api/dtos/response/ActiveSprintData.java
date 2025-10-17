package com.dev.tasktrackr.project.api.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActiveSprintData {
    private int totalStories;
    private int finishedStories;
    private int totalPoints;
    private int finishedPoints;
    private int daysLeft;
    private int averageDailyVelocity;
}
