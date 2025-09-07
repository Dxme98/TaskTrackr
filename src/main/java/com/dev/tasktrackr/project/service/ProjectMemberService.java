package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectMemberDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import org.springframework.data.domain.Page;

public interface ProjectMemberService {
    void removeMemberFromProject(String jwtUserId, Project projectId, Long memberId);
    Page<ProjectMemberDto> getAllProjectMembers(String jwtUserId, Project projectId);
}
