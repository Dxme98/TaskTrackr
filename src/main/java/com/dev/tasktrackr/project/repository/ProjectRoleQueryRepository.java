package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRoleQueryRepository extends ReadOnlyRepository<ProjectRole, Integer> {
    @EntityGraph(attributePaths = {"permissions"})
    @Query("SELECT pr FROM ProjectRole pr  WHERE pr.id = :roleId")
    ProjectRole findProjectRoleWithPermissions(@Param("roleId") int roleId);
}
