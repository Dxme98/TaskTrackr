package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;

import java.util.List;

public interface ProjectService {
    ProjectOverviewDto createProject(String userId, ProjectRequest projectDto );
    List<ProjectOverviewDto> findProjectsByUserId(String userId);
}
