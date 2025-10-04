package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.domain.basic.BasicDetails;
import com.dev.tasktrackr.project.domain.enums.Priority;
import com.dev.tasktrackr.project.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
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
    @JdbcType(PostgreSQLEnumJdbcType.class)
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
                .status(Status.IN_PROGRESS)
                .dueDate(createTaskRequest.getDueDate())
                .createdBy(taskCreator)
                .assignedMembers(assignedMembers)
                .build();
    }

    public Task complete() {
        this.status = Status.COMPLETED;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;

        return Objects.equals(basicDetails, task.basicDetails) &&
                Objects.equals(title, task.title) &&
                Objects.equals(description, task.description) &&
                priority == task.priority &&
                status == task.status &&
                Objects.equals(dueDate, task.dueDate) &&
                Objects.equals(assignedMembers, task.assignedMembers) &&
                Objects.equals(createdBy, task.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basicDetails, title, description, priority, status, dueDate, assignedMembers, createdBy);
    }
 }

