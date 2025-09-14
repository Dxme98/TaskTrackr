package com.dev.tasktrackr.project.api.dtos.request;

import com.dev.tasktrackr.project.domain.enums.Priority;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTaskRequest {
    private String title;
    private String description;
    private Priority priority;
    private LocalDateTime dueDate;
    private Set<Long> assignedToMemberIds = new HashSet<>();
}
