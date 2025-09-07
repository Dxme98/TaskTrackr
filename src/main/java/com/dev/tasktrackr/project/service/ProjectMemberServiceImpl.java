package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMemberDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.repository.ProjectMemberQueryRepository;
import com.dev.tasktrackr.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService{
    private final ProjectMemberQueryRepository projectMemberQueryRepository;
    private final ProjectRepository projectRepository;

    @Override
    public void removeMemberFromProject(String jwtUserId, Project projectId, Long memberId) {

    }

    @Override
    public Page<ProjectMemberDto> getAllProjectMembers(String jwtUserId, Project projectId) {
        return null;
    }
}
