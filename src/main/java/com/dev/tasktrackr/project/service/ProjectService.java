package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectDto;

public interface ProjectService {
    ProjectDto.Response createProject(String userId, ProjectDto.Request projectDto );
}
