package com.dev.tasktrackr.basicdetails.repository;

import com.dev.tasktrackr.scrumdetails.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
