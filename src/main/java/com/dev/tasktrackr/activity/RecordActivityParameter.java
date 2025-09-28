package com.dev.tasktrackr.activity;

public record RecordActivityParameter(
        Long projectId,
        ActivityType activityType,
        Long actorId,
        String actorName,
        Long targetId,
        String targetName,
        TargetType targetType,
        String context
) {}
