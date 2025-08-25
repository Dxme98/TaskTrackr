package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.ids.ProjectId;
import com.dev.tasktrackr.project.repository.ProjectMemberRepository;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserId;
import org.springframework.stereotype.Service;

@Service
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }


    public ProjectMember createProjectMember(UserEntity userEntity, Project project)  {
        ProjectMember createdProjectMember = new ProjectMember(userEntity, project);
        project.getProjectMembers().add(createdProjectMember);
        return projectMemberRepository.save(createdProjectMember);
    }
}
