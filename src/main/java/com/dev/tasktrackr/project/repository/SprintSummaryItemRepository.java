package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.scrum.Sprint;
import com.dev.tasktrackr.project.domain.scrum.SprintSummaryItem;
import com.dev.tasktrackr.project.domain.scrum.UserStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SprintSummaryItemRepository extends JpaRepository<SprintSummaryItem, Long> {
    Optional<SprintSummaryItem> findSprintSummaryItemBySprintIdAndUserStoryId(Long sprintId, Long userStoryId);
}
