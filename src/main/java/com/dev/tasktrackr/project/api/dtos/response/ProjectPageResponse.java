package com.dev.tasktrackr.project.api.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated response containing projects")
public class ProjectPageResponse extends PageResponse<ProjectOverviewDto> {
    public ProjectPageResponse(List<ProjectOverviewDto> content, int page, int size,
                               long totalElements, int totalPages, boolean last) {
        super(content, page, size, totalElements, totalPages, last);
    }
}
