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
}

