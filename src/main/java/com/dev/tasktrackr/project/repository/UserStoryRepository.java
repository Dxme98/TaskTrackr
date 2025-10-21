package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.api.dtos.response.UserStoryResponseDto;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

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


    boolean existsByTitleAndScrumDetailsId(@Param("title")String title, @Param("projectId")Long scrumDetailsId);

    @EntityGraph(attributePaths = {"sprintBacklogItem"})
    @Query("SELECT us FROM UserStory us WHERE us.id IN :ids AND us.scrumDetails.project.id = :projectId")
    List<UserStory> findAllByIdsAndProjectId(
            @Param("ids") Set<Long> ids,
            @Param("projectId") Long projectId
    );
}
