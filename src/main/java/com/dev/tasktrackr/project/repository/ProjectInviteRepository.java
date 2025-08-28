package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectInvite;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectInviteRepository extends ReadOnlyRepository<ProjectInvite, Long> {
}
