package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.scrum.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
