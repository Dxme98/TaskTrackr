package com.dev.tasktrackr.project.domain.scrum;

import com.dev.tasktrackr.project.api.dtos.request.CreateCommentRequest;
import com.dev.tasktrackr.project.domain.ProjectMember;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
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
        userStory.updateStatus(StoryStatus.SPRINT_BACKLOG);
        return SprintBacklogItem.builder()
                .userStory(userStory)
                .sprint(sprint)
                .assignedMembers(new HashSet<>())
                .comments(new HashSet<>())
                .build();
    }

    SprintBacklogItem assignMember(ProjectMember member) {
        assignedMembers.add(member);
        return this;
    }

    SprintBacklogItem unassignMember(ProjectMember member) {
        assignedMembers.remove(member);
        return this;
    }

    SprintBacklogItem addComment(ProjectMember member, CreateCommentRequest commentRequest) {
        Comment comment = Comment.createComment(member, commentRequest, this);
        this.comments.add(comment);
        return this;
    }

    SprintBacklogItem addBlocker(ProjectMember member, CreateCommentRequest commentRequest) {
        Comment comment = Comment.createBlocker(member, commentRequest, this);
        this.comments.add(comment);
        return this;
    }

    void removeComment(Long commentId) {
        this.comments.removeIf(comment -> comment.getId().equals(commentId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SprintBacklogItem that = (SprintBacklogItem) o;
        // Die Gleichheit wird ausschließlich durch die zugehörige UserStory bestimmt.
        return Objects.equals(userStory, that.userStory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userStory);
    }
}

