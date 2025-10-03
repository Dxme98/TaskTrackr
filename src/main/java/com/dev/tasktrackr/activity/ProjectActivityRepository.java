package com.dev.tasktrackr.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ProjectActivityRepository extends JpaRepository<ProjectActivity, Long> {
    @Query("""
        SELECT new com.dev.tasktrackr.activity.ProjectActivityDto (
            pa.id,
            pa.activityType,
            pa.actorName,
            pa.targetName,
            pa.context,
            pa.createdAt
        )
        FROM ProjectActivity pa
        WHERE pa.projectId = :projectId
    """)
    Page<ProjectActivityDto> findActivitiesByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    List<ProjectActivity> findByProjectId(Long projectId);
}
