package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.scrum.Sprint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SprintQueryRepository extends JpaRepository<Sprint, Long> {


    @Query("SELECT s FROM Sprint s WHERE s.scrumDetails.id = :projectId")
    Page<Sprint > findSprintsByProjectId(Long projectId,  Pageable pageable);
}
