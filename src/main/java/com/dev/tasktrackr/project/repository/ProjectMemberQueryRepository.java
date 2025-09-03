package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberQueryRepository extends ReadOnlyRepository<ProjectMember, Long> {
    boolean existsByUserIdAndProjectId(String userId, long projectId);

    @EntityGraph(attributePaths = {"projectRole", "projectRole.permissions"})
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.user.id = :userId AND pm.project.id = :projectId")
    ProjectMember findProjectMemberWithRoleAndPermissions(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
