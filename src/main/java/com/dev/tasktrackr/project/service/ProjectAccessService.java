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

import java.util.Set;

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

    public Project findProjectByIdWithRoles(Long projectId) {
        return projectRepository.findProjectWithRoles(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    public ProjectMember findProjectMemberWithPermissionsRolesAndUser(String userId, Long projectId) {
        return projectMemberQueryRepository.findProjectMemberWithPermissionsRolesAndUser(projectId, userId)
                .orElseThrow(() -> new UserNotProjectMemberException(userId));
    }

    public ProjectMember findProjectMemberWithPermissionsRolesAndUser(Long memberId, Long projectId) {
        return projectMemberQueryRepository.findProjectMemberWithPermissionsRolesAndUser(projectId, memberId)
                .orElseThrow(() -> new ProjectMemberNotFoundException(memberId));
    }

    // loads plain user without extra fetches
    public ProjectMember findProjectMember(Long memberId, Long projectId) {
        return projectMemberQueryRepository.findProjectMemberByIdAndProjectId(memberId, projectId)
                .orElseThrow(() -> new ProjectMemberNotFoundException(memberId));
    }

    public ProjectMember findProjectMember(String userId, Long projectId) {
        return projectMemberQueryRepository.findProjectMemberByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new ProjectMemberNotFoundException(projectId));
    }

    public Set<ProjectMember> findProjectMembers(Set<Long> memberIds) {
        return projectMemberQueryRepository.findByIdIn(memberIds);
    }

    public void checkProjectMemberShip(Long projectId, String userId) {
        if(!projectMemberQueryRepository.existsByUserIdAndProjectId(userId, projectId)) {
            throw new UserNotProjectMemberException(userId);
        }
    }
}
