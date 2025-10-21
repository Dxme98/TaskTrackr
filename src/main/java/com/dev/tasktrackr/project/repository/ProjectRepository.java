package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT DISTINCT p FROM Project p, ProjectMember pm  WHERE p.id = pm.project.id AND pm.user.id = :userId")
    Page<Project> findProjectsByUserId(@Param("userId")String userId, Pageable pageable);

    @EntityGraph(attributePaths = {"projectMembers", "projectInvites"})
    @Query("SELECT p from Project p where p.id = :projectId")
    Optional<Project> findProjectWithInvitesAndMember(@Param("projectId")Long projectId);

    @EntityGraph(attributePaths = {"projectRoles"})
    @Query("SELECT p from Project p where p.id = :projectId")
    Optional<Project> findProjectWithRoles(@Param("projectId") Long projectId);
}


