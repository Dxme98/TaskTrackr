package com.dev.tasktrackr.scrumdetails.api.dtos.response;

import com.dev.tasktrackr.project.api.dtos.response.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated response containing Userstories")
public class UserStoryPageResponse extends PageResponse<UserStoryResponseDto> {
    public UserStoryPageResponse(List<UserStoryResponseDto> content, int page, int size,
                               long totalElements, int totalPages, boolean last) {
        super(content, page, size, totalElements, totalPages, last);
    }
}
