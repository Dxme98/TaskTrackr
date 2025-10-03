package com.dev.tasktrackr.activity;

import com.dev.tasktrackr.project.api.dtos.response.PageResponse;

import java.util.List;

public class ProjectActivityPageResponse extends PageResponse<ProjectActivityDto> {
    public ProjectActivityPageResponse(List<ProjectActivityDto> content, int page, int size, long totalElements, int totalPages, boolean last) {
        super(content, page, size, totalElements, totalPages, last);
    }
}
