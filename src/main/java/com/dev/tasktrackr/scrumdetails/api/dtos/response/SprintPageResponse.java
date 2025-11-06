package com.dev.tasktrackr.scrumdetails.api.dtos.response;

import com.dev.tasktrackr.project.api.dtos.response.PageResponse;

import java.util.List;

public class SprintPageResponse extends PageResponse<SprintResponseDto> {
    public SprintPageResponse(List<SprintResponseDto> content, int page, int size, long totalElements, int totalPages, boolean last) {
        super(content, page, size, totalElements, totalPages, last);
    }
}