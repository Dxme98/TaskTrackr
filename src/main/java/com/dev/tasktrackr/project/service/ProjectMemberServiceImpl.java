package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import com.dev.tasktrackr.project.api.dtos.mapper.ProjectMemberMapper;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.repository.ProjectMemberQueryRepository;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService{
    private final ProjectMemberQueryRepository projectMemberQueryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberMapper projectMemberMapper;

    @Override
    @Transactional
    public void removeMemberFromProject(String jwtUserId, Long projectId, Long memberId) {
        Project project = projectRepository.findProjectWithInvitesAndMember(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        ProjectMember member = project.findProjectMember(jwtUserId);
        member.canRemoveUser();

        project.removeMember(memberId);
        projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectMemberDto> getAllProjectMembers(String jwtUserId, Long projectId, Pageable pageable) {

        Project project = projectRepository.findProjectWithInvitesAndMember(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        project.findProjectMember(jwtUserId); // checks if user is part of projects

        Page<ProjectMember> projectMembers = projectMemberQueryRepository.findAllProjectMembersByProjectId(projectId, pageable);



        return projectMembers.map(projectMemberMapper::toResponse);
    }
}
