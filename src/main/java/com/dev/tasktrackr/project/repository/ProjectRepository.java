package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT DISTINCT p FROM Project p, ProjectMember pm LEFT JOIN FETCH p.projectType WHERE p.id = pm.project.id AND pm.user.id = :userId")
    List<Project> findProjectsByUserId(@Param("userId")String userId);

}


