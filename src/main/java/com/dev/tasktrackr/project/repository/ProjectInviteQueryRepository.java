package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectInvite;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectInviteQueryRepository extends ReadOnlyRepository<ProjectInvite, Long> {
}
