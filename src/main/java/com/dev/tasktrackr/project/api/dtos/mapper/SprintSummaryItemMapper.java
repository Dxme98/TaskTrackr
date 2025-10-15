package com.dev.tasktrackr.project.api.dtos.mapper;

import com.dev.tasktrackr.project.api.dtos.response.SprintSummaryItemResponse;
import com.dev.tasktrackr.project.domain.scrum.SprintSummaryItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SprintSummaryItemMapper {

    SprintSummaryItemResponse toDto(SprintSummaryItem sprintSummaryItem);
}
