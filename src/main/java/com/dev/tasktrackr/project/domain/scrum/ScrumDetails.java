package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateSprintRequest;
import com.dev.tasktrackr.project.api.dtos.request.CreateUserStoryRequest;
import com.dev.tasktrackr.project.domain.Project;
import com.dev.tasktrackr.project.domain.ProjectMember;
import com.dev.tasktrackr.project.domain.basic.BasicDetails;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.NoActiveSprintFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.ProjectNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.SprintNotFoundException;
import com.dev.tasktrackr.shared.exception.custom.NotFoundExceptions.UserStoryNotFoundException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


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
    private Set<UserStory> userStories = new HashSet<>();

    @OneToMany(mappedBy = "scrumDetails", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Sprint> sprints = new HashSet<>();

    public ScrumDetails(Project project) {
        this.project = project;
    }


    public UserStory createUserStory(CreateUserStoryRequest createUserStoryRequest) {
        UserStory userStory = UserStory.create(createUserStoryRequest, this);

        userStories.add(userStory);

        return userStory;
    }

    public UserStory findUserStoryByTitle(String title) {
        return userStories.stream()
                .filter(userStory -> userStory.getTitle().equals(title))
                .findFirst()
                .orElseThrow(() -> new UserStoryNotFoundException(title));
    }

    public Sprint createSprint(CreateSprintRequest createSprintRequest) {
        Sprint createdSprint = Sprint.create(createSprintRequest, this);
        sprints.add(createdSprint);

        return createdSprint;
    }

    public List<UserStory> findUserStoriesByIds(Set<Long> ids) {
        Map<Long, UserStory> userStoryMap = this.userStories.stream()
                .collect(Collectors.toMap(UserStory::getId, Function.identity()));

        return ids.stream()
                .map(id -> {
                    UserStory userStory = userStoryMap.get(id);
                    if (userStory == null) {
                        throw new UserStoryNotFoundException("UserStory with ID '" + id + "' not found.");
                    }
                    return userStory;
                })
                .collect(Collectors.toList());
    }

    public SprintBacklogItem updateBacklogItemStatusInActiveSprint(Long backlogItemId, StoryStatus newStatus) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.updateBacklogItemStatus(backlogItemId, newStatus);
    }

    public SprintBacklogItem assignMemberToStory(Long backlogItemId, ProjectMember member) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.assignMemberToStory(backlogItemId, member);
    }

    public SprintBacklogItem unassignMemberFromStory(Long backlogItemId, ProjectMember member) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.unassignMemberFromStory(backlogItemId, member);
    }

    public SprintBacklogItem addCommentToStory(Long backlogItemId, ProjectMember member, CreateCommentRequest commentRequest) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.addCommentToStory(backlogItemId, member, commentRequest);
    }

    public SprintBacklogItem addBlockerToStory(Long backlogItemId, ProjectMember member, CreateCommentRequest commentRequest) {
        Sprint activeSprint = findActiveSprint();
        return activeSprint.addBlockerToStory(backlogItemId, member, commentRequest);
    }

    public Sprint startSprint(Long sprintId) {
        Sprint sprintToStart = findSprintById(sprintId);
        return  sprintToStart.start();
    }

    public Sprint endSprint(Long sprintId) {
        Sprint sprintToEnd = findSprintById(sprintId);
        return  sprintToEnd.end();
    }

    public Sprint findActiveSprint() {
        return sprints.stream()
                .filter(s -> s.getStatus().equals(SprintStatus.ACTIVE))
                .findFirst()
                .orElseThrow(() -> new NoActiveSprintFoundException(project.getId()));
    }

    public Sprint findSprint(Sprint sprint) {
        return sprints.stream()
                .filter(s -> s.equals(sprint))
                .findFirst()
                .orElseThrow(() -> new SprintNotFoundException(sprint.getId()));
    }

    public Sprint findSprintById(Long sprintId) {
        return sprints.stream()
                .filter(s -> s.getId().equals(sprintId))
                .findFirst()
                .orElseThrow(() -> new SprintNotFoundException(sprintId));
    }

    public UserStory findUserStoryById(Long userStoryId) {
        return userStories.stream()
                .filter(s -> s.getId().equals(userStoryId))
                .findFirst()
                .orElseThrow(() -> new UserStoryNotFoundException(userStoryId));
    }
}

