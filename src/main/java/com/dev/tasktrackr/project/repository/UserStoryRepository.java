package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

    // Can be dto-projection
    /**
    @Query("SELECT u FROM UserStory u WHERE u.scrumDetails.id = :projectId")
    Page<UserStory> findUserStoriesByProjectId(@Param("projectId") Long projectId, Pageable pageable);
     */

    @Query("""
        SELECT new com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto(
            u.id,
            u.title,
            s.name,
            u.description,
            u.priority,
            u.storyPoints,
            u.createdAt,
            u.status
        )
        FROM UserStory u
        LEFT JOIN u.sprintBacklogItem sbi
        LEFT JOIN sbi.sprint s
        WHERE u.scrumDetails.id = :scrumDetailsId
""")
    Page<UserStoryResponseDto> findUserStoriesByScrumDetailsId(
            @Param("scrumDetailsId") Long scrumDetailsId,
            Pageable pageable
    );




    @Query("SELECT u FROM UserStory u WHERE u.scrumDetails.id = :projectId AND u.title = :title")
    UserStory findUserStoryByTitleAndProjectId(@Param("title")String title, @Param("projectId")Long projectId);

    boolean existsByTitleAndScrumDetailsId(@Param("title")String title, @Param("projectId")Long scrumDetailsId);
}
