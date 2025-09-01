package com.dev.tasktrackr.project.api.dtos.response;

import java.util.List;

public class ProjectInvitePageResponse extends PageResponse<ProjectInviteResponseDto> {
    public ProjectInvitePageResponse(List<ProjectInviteResponseDto> content, int page, int size,
                               long totalElements, int totalPages, boolean last) {
        super(content, page, size, totalElements, totalPages, last);
    }

}
