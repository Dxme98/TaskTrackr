package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectDto;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Override
    public ProjectDto.Response createProject(String userId, ProjectDto.Request projectDto){

        if(userId == "abc") {
        }

        return null;
    }
}
