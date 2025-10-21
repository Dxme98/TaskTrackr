package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ProjectRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectMemberQueryRepository extends ReadOnlyRepository<ProjectMember, Long> {
    @EntityGraph(attributePaths = {"projectRole", "projectRole.permissions", "user"})
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId")
    Page<ProjectMember> findAllProjectMembersByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    @EntityGraph(attributePaths = {"projectRole", "projectRole.permissions", "user"})
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId and pm.user.id = :userId")
    Optional<ProjectMember> findProjectMemberWithPermissionsRolesAndUser(@Param("projectId") Long projectId, @Param("userId") String userId);

    boolean existsByUserIdAndProjectId(String userId, Long projectId);
}
