package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberQueryRepository extends ReadOnlyRepository<ProjectMember, Long> {
    @EntityGraph(attributePaths = {"projectRole", "projectRole.permissions", "user"})
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId")
    Page<ProjectMember> findAllProjectMembersByProjectId(@Param("projectId") Long projectId, Pageable pageable);
}
