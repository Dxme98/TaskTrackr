package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectDto;
import com.dev.tasktrackr.user.UserId;

import java.util.List;

public interface ProjectService {
    ProjectDto.Response createProject(UserId userId, ProjectDto.Request projectDto );
    List<ProjectDto.Response> findProjectsByUserId(UserId userId);
}
