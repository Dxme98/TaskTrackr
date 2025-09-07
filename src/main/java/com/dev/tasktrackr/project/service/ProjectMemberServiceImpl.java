package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.ProjectMemberMapper;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.repository.ProjectMemberQueryRepository;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService{
    private final ProjectMemberQueryRepository projectMemberQueryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberMapper projectMemberMapper;

    @Override
    public void removeMemberFromProject(String jwtUserId, Long projectId, Long memberId) {
        Project project = projectRepository.findProjectWithInvitesAndMember(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId)); // optmize

        project.removeMember(memberId);
        projectRepository.save(project);
    }

    @Override
    public Page<ProjectMemberDto> getAllProjectMembers(String jwtUserId, Long projectId, Pageable pageable) {
        Page<ProjectMember> projectMembers = projectMemberQueryRepository.findAllProjectMembersByProjectId(projectId, pageable);

        return projectMembers.map(projectMemberMapper::toResponse);
    }
}
