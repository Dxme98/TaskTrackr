package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.scrum.UserStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStoryQueryRepository extends JpaRepository<UserStory, Long> {

    // Can be dto-projection
    @Query("SELECT u FROM UserStory u WHERE u.scrumDetails.id = :projectId")
    Page<UserStory> findUserStoriesByProjectId(Long projectId, Pageable pageable);
}
