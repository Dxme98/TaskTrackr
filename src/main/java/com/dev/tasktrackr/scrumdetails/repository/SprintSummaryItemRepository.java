package com.dev.tasktrackr.scrumdetails.repository;

import com.dev.tasktrackr.scrumdetails.domain.SprintSummaryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SprintSummaryItemRepository extends JpaRepository<SprintSummaryItem, Long> {
    Optional<SprintSummaryItem> findSprintSummaryItemBySprintIdAndUserStoryId(Long sprintId, Long userStoryId);
}
