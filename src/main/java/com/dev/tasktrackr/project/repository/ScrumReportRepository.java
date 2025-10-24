package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.api.dtos.response.ActiveSprintData;
import com.dev.tasktrackr.project.domain.scrum.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScrumReportRepository extends JpaRepository<Sprint, Long> {

    @Query("""
        SELECT new com.dev.tasktrackr.project.api.dtos.response.ActiveSprintData(
            COUNT(b),
            SUM(CASE WHEN b.userStory.status = 'DONE' THEN 1 ELSE 0 END),
            COALESCE(SUM(b.userStory.storyPoints), 0L),
            COALESCE(SUM(CASE WHEN b.userStory.status = 'DONE' THEN b.userStory.storyPoints ELSE 0L END), 0L),
            s.startDate,
            s.endDate
        )
        FROM Sprint s
        LEFT JOIN s.backlogItems b
        WHERE s.scrumDetails.project.id = :projectId AND s.status = 'ACTIVE'
        GROUP BY s.id, s.startDate, s.endDate
    """)
    Optional<ActiveSprintData> getActiveSprintData(Long projectId);


}
