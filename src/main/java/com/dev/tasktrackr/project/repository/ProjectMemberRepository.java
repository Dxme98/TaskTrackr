package com.dev.tasktrackr.project.repository;

import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ids.ProjectMemberId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberRepository extends CrudRepository<ProjectMember, ProjectMemberId> {
}
