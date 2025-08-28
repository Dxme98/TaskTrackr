package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.repository.ProjectMemberQueryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProjectMemberService {
    private final ProjectMemberQueryRepository projectMemberQueryRepository;

    public ProjectMemberService(ProjectMemberQueryRepository projectMemberQueryRepository) {
        this.projectMemberQueryRepository = projectMemberQueryRepository;
    }


/**
    @Transactional
    public ProjectMember createProjectMember(UserEntity userEntity, Project project)  {
        ProjectMember createdProjectMember = new ProjectMember(userEntity, project);
        project.getProjectMembers().add(createdProjectMember);

        log.info("ProjectMembership in Project {} created successfully for user: {}", project.getName(), userEntity.getUsername());

        return projectMemberRepository.save(createdProjectMember);
    }
    */


/**
    public boolean checkProjectMembership(ProjectId projectId, UserId userId) {
        if(projectMemberRepository.existsByProjectIdAndUserId(projectId.value(), userId.value())) {
            return true;
        }
        throw new UserNotProjectMemberException(userId.value());
    }
 */
}
