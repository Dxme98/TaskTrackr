package com.dev.tasktrackr.scrumdetails.api.dtos.mapper;

import com.dev.tasktrackr.scrumdetails.api.dtos.response.SprintSummaryItemResponse;
import com.dev.tasktrackr.scrumdetails.domain.SprintSummaryItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SprintSummaryItemMapper {

    SprintSummaryItemResponse toDto(SprintSummaryItem sprintSummaryItem);
}
