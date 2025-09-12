package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.response.ProjectMemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectMemberService {
    void removeMemberFromProject(String jwtUserId, Long projectId, Long memberId);
    Page<ProjectMemberDto> getAllProjectMembers(String jwtUserId, Long projectId, Pageable pageable);
}
