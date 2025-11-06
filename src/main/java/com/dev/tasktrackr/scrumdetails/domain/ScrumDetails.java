package com.dev.tasktrackr.scrumdetails.domain;

import com.dev.tasktrackr.project.domain.Project;
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

