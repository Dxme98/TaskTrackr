package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.api.dtos.response.ActiveSprintData;
import com.dev.tasktrackr.project.api.dtos.response.ScrumMemberStatisticDto;
import com.dev.tasktrackr.project.api.dtos.response.ScrumProjectStatisticsDto;
import com.dev.tasktrackr.project.domain.scrum.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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


    @Query("""
    SELECT new com.dev.tasktrackr.project.api.dtos.response.ScrumProjectStatisticsDto(
        COALESCE(COUNT(DISTINCT CASE WHEN s.status = 'DONE' THEN s.id ELSE NULL END), 0L),
        COALESCE(SUM(CASE WHEN si.userStory.status = 'DONE' THEN si.userStory.storyPoints ELSE 0L END), 0L)
    )
    FROM Sprint s
    LEFT JOIN s.backlogItems si
    LEFT JOIN si.userStory us
    WHERE s.scrumDetails.project.id = :projectId
    GROUP BY s.scrumDetails.project.id
""")
    Optional<ScrumProjectStatisticsDto> getScrumProjectStatisticsDto(Long projectId);


    @Query("""
    SELECT new com.dev.tasktrackr.project.api.dtos.response.ScrumMemberStatisticDto(
        pm.user.username,
        COUNT(sbi.id),
        COALESCE(SUM(us.storyPoints), 0L),
        COALESCE(SUM(CASE WHEN us.status = 'DONE' THEN us.storyPoints ELSE 0L END), 0L),
        (SELECT COALESCE(COUNT(c.id), 0L)
         FROM Comment c
         WHERE c.type = 'BLOCKER'
         AND c.sprintBacklogItem.sprint.status = 'ACTIVE'
         AND pm MEMBER OF c.sprintBacklogItem.assignedMembers),
 
        COUNT(CASE WHEN us.status = 'SPRINT_BACKLOG' THEN 1 ELSE NULL END),
        COUNT(CASE WHEN us.status = 'IN_PROGRESS' THEN 1 ELSE NULL END),
        COUNT(CASE WHEN us.status = 'REVIEW' THEN 1 ELSE NULL END),
        COUNT(CASE WHEN us.status = 'DONE' THEN 1 ELSE NULL END)
    )
    FROM ProjectMember pm
   
    LEFT JOIN SprintBacklogItem sbi ON (pm MEMBER OF sbi.assignedMembers AND sbi.sprint.status = 'ACTIVE')
    LEFT JOIN sbi.userStory us
    WHERE pm.project.id = :projectId
    
    GROUP BY pm.id, pm.user.username
""")
    List<ScrumMemberStatisticDto> getScrumMemberStatisticsForActiveSprint(Long projectId);





}
