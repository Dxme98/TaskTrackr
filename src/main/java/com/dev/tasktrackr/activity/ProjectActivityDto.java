package com.dev.tasktrackr.activity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectActivityDto {
    private Long id;
    private ActivityType activityType;
    private String actorName;
    private String targetName;
    private String context;
    private Instant createdAt;
}
