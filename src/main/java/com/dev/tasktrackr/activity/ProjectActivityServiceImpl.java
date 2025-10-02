package com.dev.tasktrackr.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectActivityServiceImpl implements ProjectActivityService{
    private final ProjectActivityRepository projectActivityRepository;

    @Override
    @Transactional
    public void recordActivity(RecordActivityParameter params) {
        ProjectActivity activity = new ProjectActivity();

        activity.setProjectId(params.projectId());
        activity.setActivityType(params.activityType());
        activity.setActorId(params.actorId());
        activity.setActorName(params.actorName());
        activity.setTargetId(params.targetId());
        activity.setTargetName(params.targetName());
        activity.setTargetType(params.targetType());
        activity.setContext(params.context());

        projectActivityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectActivityDto> findActivitiesByProjectId(String requestUserId, Long projectId, Pageable pageable) {
        return projectActivityRepository.findActivitiesByProjectId(projectId, pageable);
    }
}
