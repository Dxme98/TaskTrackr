package com.dev.tasktrackr.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectActivityService {
    void recordActivity(RecordActivityParameter params);
    public Page<ProjectActivityDto> findActivitiesByProjectId(Long projectId, Pageable pageable);
}
