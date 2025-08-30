package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectInvite;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectInviteQueryRepository extends ReadOnlyRepository<ProjectInvite, Long> {
    boolean existsByReceiverIdAndProjectId(String receiverId, long projectId);

    ProjectInvite findByProjectIdAndReceiverId(long projectId, String receiverId);

    @EntityGraph(attributePaths = {"receiver", "sender", "project"})
    @Query("SELECT pm FROM ProjectInvite pm WHERE pm.id = :inviteId")
    Optional<ProjectInvite> findByIdWithRelations(@Param("inviteId") Long inviteId);
}
