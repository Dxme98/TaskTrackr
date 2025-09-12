package com.dev.tasktrackr.project.api.dtos.response;

import java.util.List;

public class ProjectMemberPageResponse extends PageResponse<ProjectMemberDto>{
    public ProjectMemberPageResponse(List<ProjectMemberDto> content, int page, int size, long totalElements, int totalPages, boolean last) {
        super(content, page, size, totalElements, totalPages, last);
    }
}
