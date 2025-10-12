package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.domain.ProjectMember;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "sprint_backlog_items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @OneToMany(fetch = FetchType.LAZY)
    private Set<ProjectMember> assignedMembers = new HashSet<>();

    @OneToMany(mappedBy = "sprintBacklogItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Comment> comments;

    public static SprintBacklogItem create(UserStory userStory, Sprint sprint) {
        return SprintBacklogItem.builder()
                .userStory(userStory)
                .sprint(sprint)
                .assignedMembers(new HashSet<>())
                .comments(new HashSet<>())
                .build();
    }
}

