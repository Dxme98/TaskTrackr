package com.dev.tasktrackr.activity;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProjectActivityListener {

    private final ProjectActivityService projectActivityService;

    public ProjectActivityListener(ProjectActivityService projectActivityService) {
        this.projectActivityService = projectActivityService;
    }

    @EventListener
    public void handleActivityEvent(ActivityEvent event) {
        projectActivityService.recordActivity(event.toActivityParameter());
    }
}
