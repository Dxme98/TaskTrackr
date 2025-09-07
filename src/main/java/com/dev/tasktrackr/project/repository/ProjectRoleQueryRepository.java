package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.api.dtos.response.ProjectRoleResponse;
import com.dev.tasktrackr.project.domain.ProjectRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRoleQueryRepository extends ReadOnlyRepository<ProjectRole, Integer>{


    @EntityGraph(attributePaths = "permissions")
    Page<ProjectRole> findAllByProjectId(@Param("projectId")Long projectId, Pageable pageable);
}
