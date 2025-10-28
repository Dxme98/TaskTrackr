package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto;
import com.dev.tasktrackr.project.domain.ProjectInvite;
import com.dev.tasktrackr.project.domain.enums.ProjectInviteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectInviteRepository extends JpaRepository<ProjectInvite, Long> {
    @EntityGraph(attributePaths = {"receiver", "sender", "project", "project.projectMembers"})
    @Query("SELECT pm FROM ProjectInvite pm WHERE pm.id = :inviteId")
    Optional<ProjectInvite> findByIdWithRelations(@Param("inviteId") Long inviteId);

    @Query("""
        SELECT new com.dev.tasktrackr.project.api.dtos.response.ProjectInviteResponseDto(
            pi.id,
            pi.sender.id,
            pi.sender.username,
            pi.receiver.id,
            pi.receiver.username,
            pi.project.id,
            pi.project.name,
            pi.inviteStatus,
            pi.createdAt,
            pi.updatedAt
        )
        FROM ProjectInvite pi
        WHERE pi.receiver.id=:receiverId AND pi.inviteStatus=:inviteStatus
""")
    Page<ProjectInviteResponseDto> findProjectInvitesByReceiverIdAndInviteStatus(@Param("receiverId") String receiverId,
                                                                                 @Param("inviteStatus")ProjectInviteStatus inviteStatus,
                                                                                 Pageable pageable);

    ProjectInvite findProjectInviteByProjectIdAndReceiverId(Long projectId, String receiverId);

    boolean existsByProjectIdAndReceiverIdAndInviteStatus(Long projectId, String receiverId, ProjectInviteStatus inviteStatus);
}
