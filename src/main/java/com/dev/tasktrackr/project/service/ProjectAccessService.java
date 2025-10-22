package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.repository.ProjectMemberQueryRepository;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.UserNotProjectMemberException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectMemberNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectAccessService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberQueryRepository projectMemberQueryRepository;

    public Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    public ProjectMember findProjectMemberWithPermissionsRolesAndUser(String userId, Long projectId) {
        return projectMemberQueryRepository.findProjectMemberWithPermissionsRolesAndUser(projectId, userId)
                .orElseThrow(() -> new UserNotProjectMemberException(userId));
    }

    // loads plain user without extra fetches
    public ProjectMember findProjectMember(Long memberId, Long projectId) {
        return projectMemberQueryRepository.findProjectMemberByIdAndProjectId(memberId, projectId)
                .orElseThrow(() -> new ProjectMemberNotFoundException(memberId));
    }

    public void checkProjectMemberShip(Long projectId, String userId) {
        if(!projectMemberQueryRepository.existsByUserIdAndProjectId(userId, projectId)) {
            throw new UserNotProjectMemberException(userId);
        }
    }
}
