package com.dev.tasktrackr.project.api.dtos.response;

import java.util.List;

public class ProjectRolePageResponse extends PageResponse<ProjectRoleResponse>{
    public ProjectRolePageResponse(List<ProjectRoleResponse> content, int page, int size, long totalElements, int totalPages, boolean last) {
        super(content, page, size, totalElements, totalPages, last);
    }
}
