package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.scrum.CommentType;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommentResponseDto {
    private Long id;
    private Long sprintBacklogItemId;
    private String createdByUsername;
    private String message;
    private CommentType type;
    private Instant createdAt;
}
