package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectTypeRepository extends CrudRepository<ProjectType, Integer> {
}
