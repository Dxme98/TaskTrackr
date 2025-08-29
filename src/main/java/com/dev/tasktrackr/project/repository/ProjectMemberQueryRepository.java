package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectMember;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberQueryRepository extends ReadOnlyRepository<ProjectMember, Long> {
    boolean existsByUserIdAndProjectId(String userId, long projectId);
}
