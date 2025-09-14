package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private BasicDetails basicDetails;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private ProjectMember createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private ProjectMember updatedBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_assignments",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<ProjectMember> assignedMembers = new HashSet<>();

    public static Task create(CreateTaskRequest createTaskRequest, BasicDetails basicDetails,
                              ProjectMember taskCreator, Set<ProjectMember> assignedMembers) {

        return Task.builder()
                .basicDetails(basicDetails)
                .title(createTaskRequest.getTitle())
                .description(createTaskRequest.getDescription())
                .priority(createTaskRequest.getPriority())
                .dueDate(createTaskRequest.getDueDate())
                .createdBy(taskCreator)
                .assignedMembers(assignedMembers)
                .build();
    }
 }

