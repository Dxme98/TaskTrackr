package com.dev.tasktrackr.project.api.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectTypeDto {
    private Integer id;
    private String name;
}
