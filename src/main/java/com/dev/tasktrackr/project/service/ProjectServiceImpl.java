package com.dev.tasktrackr.project.service;

import com.dev.tasktrackr.project.api.dtos.ProjectDto;
import com.dev.tasktrackr.shared.ErrorCode;
import com.dev.tasktrackr.shared.ForbiddenException;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Override
    public ProjectDto.Response createProject(String userId, ProjectDto.Request projectDto){

        if(userId == "abc") {
            throw new ForbiddenException("User is not allowed to write project", ErrorCode.NO_WRITE_PERMISSION);
        }

        return null;
    }
}
