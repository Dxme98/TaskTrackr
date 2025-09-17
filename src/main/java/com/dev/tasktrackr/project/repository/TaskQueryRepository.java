package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.BasicDetails;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.Task;
import com.dev.tasktrackr.project.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TaskQueryRepository extends ReadOnlyRepository<Task, Long>{

    @Query("SELECT t FROM Task t WHERE t.basicDetails.project.id = :projectId AND t.status = :status")
    Page<Task> findAllByStatus(@Param("projectId") Long projectId, @Param("status") Status status, Pageable pageable);

    @Query("""
    SELECT t 
    FROM Task t 
    JOIN t.assignedMembers m
    WHERE t.basicDetails.project.id = :projectId
      AND m.id = :memberId
    """)
    Page<Task> findAllByMember(@Param("projectId") Long projectId, @Param("memberId") Long memberId, Pageable pageable);

    @Query("""
        SELECT DISTINCT t
        FROM Task t
        JOIN t.assignedMembers m
        WHERE t.basicDetails.project.id = :projectId
          AND m.user.id = :userId AND t.status = 'IN_PROGRESS'
        """)
    @EntityGraph(attributePaths = {"assignedMembers", "createdBy"})
    Page<Task> findAllTasksByAssignedUserId(
            @Param("projectId") Long projectId,
            @Param("userId") String userId,
            Pageable pageable
    );

    @Query("SELECT t FROM Task t WHERE t.basicDetails.project.id = :projectId")
    Page<Task> findAllByProjectId(@Param("projectId") Long projectId, Pageable pageable);
}
