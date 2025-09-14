package com.dev.tasktrackr.project.domain;

import com.dev.tasktrackr.project.api.dtos.request.CreateLinkRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateTaskRequest;
import com.dev.tasktrackr.project.api.dtos.request.UpdateInformationContentRequest;
import com.dev.tasktrackr.shared.exception.custom.AccessDeniedExceptions.ProjectMemberNotAllowedToCompleteTaskException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.LinkNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.TaskNotFoundException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "basic_details")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BasicDetails {
    @Id
    private Long id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "project_id")
    private Project project;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "basicDetails")
    private Set<Task> tasks = new HashSet<>();
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "basicDetails")
    private Set<Link> links = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "basicDetails")
    private Information information;

    public BasicDetails (Project project) {
        this.project = project;
        this.information = new Information(this);
    }

    public Task addTask(CreateTaskRequest createTaskRequest, ProjectMember taskCreator, Set<ProjectMember> assignedMembers) {
        Task createdTask = Task.create(createTaskRequest, this, taskCreator, assignedMembers);
        tasks.add(createdTask);

        return createdTask;
    }

    public Task completeTask(Long taskId, Long memberId) {
        Task task = findTask(taskId);

        memberIsAllowedToCompleteTask(task, memberId);

        return task.complete();
    }

    public void deleteTask(Long taskId) {
        Task task = findTask(taskId);
        tasks.remove(task);
    }

    private void memberIsAllowedToCompleteTask(Task task, Long memberId) {

        // Creator is allowed to complete task
        if(task.getCreatedBy().getId().equals(memberId)) {
            return;
        }

        // Assigned members are allowed to complete task
        task.getAssignedMembers().stream()
                .filter(assignedMember -> assignedMember.getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new ProjectMemberNotAllowedToCompleteTaskException(memberId));
    }

    public Task findTask(Long taskId) {
        return this.tasks.stream()
                .filter(task -> task.getId().equals(taskId)).findFirst()
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    public Information updateInformationContent(UpdateInformationContentRequest updateInformationContentRequest) {
        information.updateContent(updateInformationContentRequest.getContent());
        return information;
    }

    public Link addLink(CreateLinkRequest linkRequest) {
        Link createdLink = new Link(linkRequest.getTitle(), linkRequest.getTitle(), linkRequest.getLinkType());
        links.add(createdLink);

        return createdLink;
    }

    public void deleteLink(Long linkId) {
        Link toDelete = findLink(linkId);
        links.remove(toDelete);
    }

    public Link findLink(Long linkId) {
        return this.links.stream()
                .filter(link -> link.getId().equals(linkId))
                .findFirst()
                .orElseThrow(() -> new LinkNotFoundException(linkId));
    }
}
