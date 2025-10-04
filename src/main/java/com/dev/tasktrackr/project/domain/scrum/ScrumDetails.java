package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.domain.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
    private List<Sprint> sprints = new ArrayList<>();

    public ScrumDetails(Project project) {
        this.project = project;
    }
}

