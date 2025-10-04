package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.domain.ProjectMember;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "sprint_backlog_items")
@Getter
public class SprintBacklogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_story_id", nullable = false, unique = true)
    private UserStory userStory;

    @JoinTable(
            name = "sprint_backlog_item_assignee",
            joinColumns = @JoinColumn(name = "sprint_backlog_item_id"),
            inverseJoinColumns = @JoinColumn(name = "assigned_member_id")
    )
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ProjectMember> assignedMembers = new HashSet<>();

    // Comments missing
}

