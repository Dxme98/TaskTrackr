package com.dev.tasktrackr.project.api.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScrumMemberStatisticDto {
    String username;
    int totalTasks;
    int finishedTasks;
    int finishedTasksPercentage;
    int totalPoints;
    int finishedPoints;
    int totalBlocker;
    int tasksInBacklog;
    int tasksInProgress;
    int tasksInReview;
    int tasksInDone;
}
