package com.dev.tasktrackr.project.api.dtos.response;

import java.util.List;

public class SprintPageResponse extends PageResponse<SprintResponseDto> {
    public SprintPageResponse(List<SprintResponseDto> content, int page, int size, long totalElements, int totalPages, boolean last) {
        super(content, page, size, totalElements, totalPages, last);
    }
}