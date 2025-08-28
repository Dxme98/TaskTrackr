package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectType;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectTypeQueryRepository extends ReadOnlyRepository<ProjectType, Integer> {
}
