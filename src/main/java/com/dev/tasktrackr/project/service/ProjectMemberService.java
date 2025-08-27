package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ids.ProjectId;
import com.dev.tasktrackr.project.repository.ProjectMemberRepository;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserId;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }


    @Transactional
    public ProjectMember createProjectMember(UserEntity userEntity, Project project)  {
        ProjectMember createdProjectMember = new ProjectMember(userEntity, project);
        project.getProjectMembers().add(createdProjectMember);

        log.info("ProjectMembership in Project {} created successfully for user: {}", project.getName(), userEntity.getUsername());

        return projectMemberRepository.save(createdProjectMember);
    }
}
