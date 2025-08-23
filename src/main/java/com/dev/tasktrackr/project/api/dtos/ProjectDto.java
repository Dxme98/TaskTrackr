package com.dev.tasktrackr.project.api.dtos;

import com.dev.tasktrackr.project.domain.ProjectType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public sealed interface ProjectDto permits ProjectDto.Request, ProjectDto.Response{
    record Request(
            @NotBlank(message = "Project name is required")
            @Size(min = 3, max = 255, message = "Project name must be between 3 and 255 characters")
            String name,

            @NotNull(message = "Project type is required")
            @Min(value = 1, message = "Invalid project type")
            ProjectType projectTypeId
    ) implements ProjectDto {}

    record Response(
            Long id,
            String name,
            LocalDateTime createdAt
         //   ProjectTypeDto projectType,
         //   List<ProjectMemberDto> members
    ) implements ProjectDto {}
}
