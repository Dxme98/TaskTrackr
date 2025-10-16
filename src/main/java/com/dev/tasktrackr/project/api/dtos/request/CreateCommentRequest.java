package com.dev.tasktrackr.project.api.dtos.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CreateCommentRequest {
    String message;
}
