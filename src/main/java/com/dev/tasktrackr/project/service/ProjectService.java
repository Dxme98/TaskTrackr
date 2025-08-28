package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectTypeDto;
import com.dev.tasktrackr.project.api.dtos.request.ProjectRequest;
import com.dev.tasktrackr.project.api.dtos.response.ProjectDetailsDto;
import com.dev.tasktrackr.project.api.dtos.response.ProjectOverviewDto;
import com.dev.tasktrackr.project.domain.ProjectType;
import com.dev.tasktrackr.project.domain.ids.ProjectId;
import com.dev.tasktrackr.user.UserDto;
import com.dev.tasktrackr.user.UserEntity;
import com.dev.tasktrackr.user.UserId;
import org.mapstruct.Mapping;

import java.util.List;

public interface ProjectService {
    ProjectOverviewDto createProject(UserId userId, ProjectRequest projectDto );
    List<ProjectOverviewDto> findProjectsByUserId(UserId userId);
}
