package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.scrum.Sprint;
import com.dev.tasktrackr.project.domain.scrum.SprintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SprintQueryRepository extends JpaRepository<Sprint, Long> {


    @Query("SELECT s FROM Sprint s WHERE s.scrumDetails.id = :projectId AND s.status = :status")
    Page<Sprint > findSprintsByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") SprintStatus status, Pageable pageable);
}
