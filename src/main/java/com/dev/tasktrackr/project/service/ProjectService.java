package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {
    ProjectOverviewDto createProject(String userId, ProjectRequest projectDto );
    Page<ProjectOverviewDto> findProjectsByUserId(String userId, PageRequest pageRequest);
    ProjectOverviewDto getProjectDetails(Long projectId, String userId);
}
