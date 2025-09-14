package com.dev.tasktrackr.project.api.dtos.response;

import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.enums.Status;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponseDto {
    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProjectMemberDto createdBy;
    private ProjectMemberDto updatedBy;
    private Set<String> assignedToMemberUsernames = new HashSet<>();
}
