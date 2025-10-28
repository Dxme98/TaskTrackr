package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.Task;
import com.dev.tasktrackr.project.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.basicDetails.project.id = :projectId AND t.status = :status")
    @EntityGraph(attributePaths = {"assignedMembers", "createdBy"})
    Page<Task> findAllByStatus(@Param("projectId") Long projectId, @Param("status") Status status, Pageable pageable);

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
    @EntityGraph(attributePaths = {"assignedMembers", "createdBy"})
    Page<Task> findAllByProjectId(@Param("projectId") Long projectId, Pageable pageable);


    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Task t " +
            "LEFT JOIN t.assignedMembers am " +
            "WHERE t.id = :taskId AND (t.createdBy.id = :memberId OR am.id = :memberId)")
    boolean isMemberAllowedToCompleteTask(@Param("taskId") Long taskId, @Param("memberId") Long memberId);
}
