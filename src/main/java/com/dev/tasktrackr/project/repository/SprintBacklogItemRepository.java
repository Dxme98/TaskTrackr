package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.scrum.SprintBacklogItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SprintBacklogItemRepository extends JpaRepository<SprintBacklogItem, Long> {

    @EntityGraph(attributePaths = {"userStory", "sprint", "assignedMembers", "comments"})
    Optional<SprintBacklogItem> findSprintBacklogItemById(Long id);
}
