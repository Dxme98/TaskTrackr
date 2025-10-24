package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.api.dtos.response.ScrumMemberStatisticDto;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.shared.exception.custom.ConflictExceptions.SprintNotActiveException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.NoActiveSprintFoundException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;


@Entity
@Table(name = "scrum_details")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScrumDetails {
    @Id
    private Long id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "scrumDetails", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserStory> userStories = new ArrayList<>();

    @OneToMany(mappedBy = "scrumDetails", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Sprint> sprints = new HashSet<>();

    public ScrumDetails(Project project) {
        this.project = project;
    }


    public UserStory createUserStory(CreateUserStoryRequest createUserStoryRequest) {
        return UserStory.create(createUserStoryRequest, this);
    }


    public Sprint createSprint(CreateSprintRequest createSprintRequest) {
        Sprint createdSprint = Sprint.create(createSprintRequest, this);
        sprints.add(createdSprint);

        return createdSprint;
    }

    public SprintBacklogItem updateBacklogItemStatus(SprintBacklogItem backlogItem, StoryStatus newStatus, Sprint sprint, SprintSummaryItem sprintSummaryItem) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

        return sprint.updateBacklogItemStatus(backlogItem, newStatus, sprintSummaryItem);
    }

    public SprintBacklogItem assignMemberToStory(SprintBacklogItem backlogItem, ProjectMember member, Sprint sprint) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

        return sprint.assignMemberToStory(backlogItem, member);
    }

    public SprintBacklogItem unassignMemberFromStory(SprintBacklogItem backlogItem, ProjectMember member, Sprint sprint) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

        return sprint.unassignMemberFromStory(backlogItem, member);
    }

    public Comment addCommentToStory(SprintBacklogItem backlogItem, ProjectMember member, CreateCommentRequest commentRequest, Sprint sprint) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

        return sprint.addCommentToStory(backlogItem, member, commentRequest);
    }

    public Comment addBlockerToStory(SprintBacklogItem backlogItem, ProjectMember member, CreateCommentRequest commentRequest, Sprint sprint) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

        return sprint.addBlockerToStory(backlogItem, member, commentRequest);
    }

    public Comment removeCommentFromStory(SprintBacklogItem backlogItem, Comment comment, Sprint sprint) {

        if(!sprint.isActive()) throw new SprintNotActiveException(sprint.getId());

       return sprint.removeCommentFromStory(backlogItem, comment);
    }

    public void startSprint(Sprint sprintToStart) {
        sprintToStart.start();
    }

    public Sprint endSprint(Sprint sprintToEnd) {
        return  sprintToEnd.end();
    }
}

