package com.dev.tasktrackr.basicdetails.api.dtos.response;

import com.dev.tasktrackr.project.api.dtos.response.PageResponse;

import java.util.List;

public class TaskPageResponse extends PageResponse<TaskResponseDto> {
    public TaskPageResponse(List<TaskResponseDto> content, int page, int size, long totalElements, int totalPages, boolean last) {
        super(content, page, size, totalElements, totalPages, last);
    }
}
